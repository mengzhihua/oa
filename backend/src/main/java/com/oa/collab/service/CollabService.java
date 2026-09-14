package com.oa.collab.service;

import com.oa.collab.dto.ExpenseRequest;
import com.oa.collab.dto.MeetingBookingRequest;
import com.oa.collab.dto.NoticeRequest;
import com.oa.collab.entity.OaExpense;
import com.oa.collab.entity.OaMeetingBooking;
import com.oa.collab.entity.OaMessage;
import com.oa.collab.entity.OaNotice;
import com.oa.collab.entity.OaNoticeRead;
import com.oa.common.BizException;
import com.oa.workflow.WorkflowService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CollabService {
    private final OaNoticeService noticeService;
    private final OaNoticeReadService noticeReadService;
    private final OaMeetingBookingService bookingService;
    private final OaMessageService messageService;
    private final OaExpenseService expenseService;
    private final WorkflowService workflowService;
    private final JdbcTemplate jdbc;

    public CollabService(OaNoticeService noticeService,
                         OaNoticeReadService noticeReadService,
                         OaMeetingBookingService bookingService,
                         OaMessageService messageService,
                         OaExpenseService expenseService,
                         WorkflowService workflowService,
                         JdbcTemplate jdbc) {
        this.noticeService = noticeService;
        this.noticeReadService = noticeReadService;
        this.bookingService = bookingService;
        this.messageService = messageService;
        this.expenseService = expenseService;
        this.workflowService = workflowService;
        this.jdbc = jdbc;
    }

    @Transactional
    public OaNotice createNotice(Long userId, NoticeRequest request) {
        OaNotice notice = new OaNotice();
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setType(request.getType());
        notice.setDeptId(request.getDeptId());
        notice.setPublisher(userId);
        notice.setPinned(request.getPinned() == null ? 0 : request.getPinned());
        notice.setStatus("DRAFT");
        notice.setReadCount(0);
        noticeService.save(notice);
        return notice;
    }

    @Transactional
    public void publishNotice(Long id) {
        OaNotice notice = noticeService.getById(id);
        if (notice == null) {
            throw new BizException("公告不存在");
        }
        notice.setStatus("PUBLISHED");
        notice.setPublishedAt(LocalDateTime.now());
        noticeService.updateById(notice);
    }

    public List<OaNotice> mineNotices(Long userId) {
        return jdbc.query("SELECT n.* FROM oa_notice n LEFT JOIN oa_notice_read r "
                        + "ON r.notice_id = n.id AND r.user_id = ? "
                        + "WHERE n.status = 'PUBLISHED' ORDER BY CASE WHEN r.id IS NULL "
                        + "THEN 0 ELSE 1 END, n.pinned DESC, n.published_at DESC",
                new Object[]{userId}, (result, rowNum) -> {
                    OaNotice notice = new OaNotice();
                    notice.setId(result.getLong("id"));
                    notice.setTitle(result.getString("title"));
                    notice.setContent(result.getString("content"));
                    notice.setType(result.getString("type"));
                    notice.setDeptId(result.getObject("dept_id", Long.class));
                    notice.setPublisher(result.getObject("publisher", Long.class));
                    notice.setPublishedAt(result.getTimestamp("published_at") == null ? null
                            : result.getTimestamp("published_at").toLocalDateTime());
                    notice.setPinned(result.getInt("pinned"));
                    notice.setStatus(result.getString("status"));
                    notice.setReadCount(result.getInt("read_count"));
                    return notice;
                });
    }

    @Transactional
    public void readNotice(Long userId, Long noticeId) {
        OaNoticeRead read = noticeReadService.lambdaQuery()
                .eq(OaNoticeRead::getUserId, userId)
                .eq(OaNoticeRead::getNoticeId, noticeId).one();
        if (read == null) {
            read = new OaNoticeRead();
            read.setUserId(userId);
            read.setNoticeId(noticeId);
            read.setReadAt(LocalDateTime.now());
            noticeReadService.save(read);
            jdbc.update("UPDATE oa_notice SET read_count = read_count + 1 WHERE id = ?", noticeId);
        }
    }

    @Transactional
    public OaMeetingBooking bookMeeting(Long userId, MeetingBookingRequest request) {
        Integer conflicts = jdbc.queryForObject(
                "SELECT COUNT(*) FROM oa_meeting_booking WHERE room_id = ? "
                        + "AND status <> 'CANCELED' AND start_time < ? AND end_time > ?",
                Integer.class, request.getRoomId(), request.getEndTime(), request.getStartTime());
        if (conflicts != null && conflicts > 0) {
            throw new BizException("会议室时间段已被预约");
        }
        OaMeetingBooking booking = new OaMeetingBooking();
        booking.setRoomId(request.getRoomId());
        booking.setOrganizer(userId);
        booking.setTitle(request.getTitle());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setAttendees(request.getAttendees());
        booking.setStatus("BOOKED");
        bookingService.save(booking);
        return booking;
    }

    @Transactional
    public OaExpense submitExpense(Long userId, ExpenseRequest request) {
        OaExpense expense = new OaExpense();
        expense.setApplicantId(userId);
        expense.setTitle(request.getTitle());
        expense.setItemsJson(request.getItemsJson());
        expense.setTotal(request.getTotal());
        expense.setAttachments(request.getAttachments());
        expense.setStatus("PENDING");
        expenseService.save(expense);
        java.util.Map<String, Object> form = new java.util.HashMap<>();
        form.put("total", request.getTotal());
        form.put("title", request.getTitle());
        java.util.Map<String, Object> instance = workflowService.start("EXPENSE", userId,
                "报销：" + request.getTitle(), form, "EXPENSE", String.valueOf(expense.getId()));
        expense.setWfInstanceId(((Number) instance.get("id")).longValue());
        expenseService.updateById(expense);
        return expense;
    }

    public List<OaMessage> messages(Long userId) {
        return messageService.lambdaQuery().eq(OaMessage::getToUserId, userId)
                .orderByDesc(OaMessage::getId).list();
    }

    public long unreadCount(Long userId) {
        return messageService.lambdaQuery().eq(OaMessage::getToUserId, userId)
                .isNull(OaMessage::getReadAt).count();
    }

    public void readMessage(Long userId, Long id) {
        OaMessage message = messageService.lambdaQuery().eq(OaMessage::getId, id)
                .eq(OaMessage::getToUserId, userId).one();
        if (message != null) {
            message.setReadAt(LocalDateTime.now());
            messageService.updateById(message);
        }
    }
}
