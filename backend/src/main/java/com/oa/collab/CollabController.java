package com.oa.collab;

import com.oa.collab.dto.ExpenseRequest;
import com.oa.collab.dto.MeetingBookingRequest;
import com.oa.collab.dto.NoticeRequest;
import com.oa.collab.dto.ScheduleRequest;
import com.oa.collab.dto.MeetingRoomRequest;
import com.oa.collab.entity.OaMeetingBooking;
import com.oa.collab.entity.OaMeetingRoom;
import com.oa.collab.entity.OaMessage;
import com.oa.collab.entity.OaNotice;
import com.oa.collab.entity.OaSchedule;
import com.oa.collab.entity.OaExpense;
import com.oa.collab.service.CollabService;
import com.oa.collab.service.OaMeetingRoomService;
import com.oa.collab.service.OaMeetingBookingService;
import com.oa.collab.service.OaExpenseService;
import com.oa.collab.service.OaScheduleService;
import com.oa.collab.vo.ContactView;
import org.springframework.jdbc.core.JdbcTemplate;
import com.oa.common.R;
import com.oa.system.auth.CurrentUser;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
public class CollabController {
    private final CollabService collabService;
    private final OaMeetingRoomService roomService;
    private final OaMeetingBookingService bookingService;
    private final OaScheduleService scheduleService;
    private final JdbcTemplate jdbc;
    private final OaExpenseService expenseService;

    public CollabController(CollabService collabService,
                            OaMeetingRoomService roomService,
                            OaMeetingBookingService bookingService,
                            OaScheduleService scheduleService,
                            JdbcTemplate jdbc,
                            OaExpenseService expenseService) {
        this.collabService = collabService;
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.scheduleService = scheduleService;
        this.jdbc = jdbc;
        this.expenseService = expenseService;
    }

    @PostMapping("/notices")
    public R<OaNotice> createNotice(@Valid @RequestBody NoticeRequest request) {
        return R.ok(collabService.createNotice(CurrentUser.id(), request));
    }

    @PostMapping("/notices/{id}/publish")
    public R<Void> publishNotice(@PathVariable Long id) {
        collabService.publishNotice(id);
        return R.ok();
    }

    @PutMapping("/notices/{id}")
    public R<OaNotice> updateNotice(@PathVariable Long id,
                                    @Valid @RequestBody NoticeRequest request) {
        return R.ok(collabService.updateNotice(id, request));
    }

    @PostMapping("/notices/{id}/revoke")
    public R<Void> revokeNotice(@PathVariable Long id) {
        collabService.revokeNotice(id);
        return R.ok();
    }

    @PostMapping("/schedules")
    public R<OaSchedule> createSchedule(@Valid @RequestBody ScheduleRequest request) {
        return R.ok(collabService.createSchedule(CurrentUser.id(), request));
    }

    @GetMapping("/notices/mine")
    public R<List<OaNotice>> mineNotices() {
        return R.ok(collabService.mineNotices(CurrentUser.id()));
    }

    @PostMapping("/notices/{id}/read")
    public R<Void> readNotice(@PathVariable Long id) {
        collabService.readNotice(CurrentUser.id(), id);
        return R.ok();
    }

    @GetMapping("/schedules/mine")
    public R<List<OaSchedule>> schedules(@RequestParam(required = false) LocalDateTime from,
                                         @RequestParam(required = false) LocalDateTime to) {
        return R.ok(scheduleService.lambdaQuery().eq(OaSchedule::getUserId, CurrentUser.id())
                .ge(from != null, OaSchedule::getStartTime, from)
                .lt(to != null, OaSchedule::getStartTime, to)
                .orderByAsc(OaSchedule::getStartTime).list());
    }

    @GetMapping("/meetings/rooms")
    public R<List<OaMeetingRoom>> rooms() {
        return R.ok(roomService.lambdaQuery().eq(OaMeetingRoom::getStatus, "ACTIVE").list());
    }

    @PostMapping("/meetings/rooms")
    public R<OaMeetingRoom> createRoom(@Valid @RequestBody MeetingRoomRequest request) {
        OaMeetingRoom room = new OaMeetingRoom();
        room.setName(request.getName());
        room.setLocation(request.getLocation());
        room.setCapacity(request.getCapacity());
        room.setEquipment(request.getEquipment());
        room.setStatus(request.getStatus() == null ? "ACTIVE" : request.getStatus());
        roomService.save(room);
        return R.ok(room);
    }

    @PostMapping("/meetings/bookings")
    public R<OaMeetingBooking> book(@Valid @RequestBody MeetingBookingRequest request) {
        return R.ok(collabService.bookMeeting(CurrentUser.id(), request));
    }

    @PostMapping("/meetings/bookings/{id}/cancel")
    public R<Void> cancelMeeting(@PathVariable Long id) {
        OaMeetingBooking booking = bookingService.getById(id);
        if (booking != null && booking.getOrganizer().equals(CurrentUser.id())) {
            booking.setStatus("CANCELED");
            bookingService.updateById(booking);
        }
        return R.ok();
    }

    @GetMapping("/messages/mine")
    public R<List<OaMessage>> messages() {
        return R.ok(collabService.messages(CurrentUser.id()));
    }

    @GetMapping("/messages/unread-count")
    public R<Long> unreadCount() {
        return R.ok(collabService.unreadCount(CurrentUser.id()));
    }

    @PostMapping("/messages/{id}/read")
    public R<Void> readMessage(@PathVariable Long id) {
        collabService.readMessage(CurrentUser.id(), id);
        return R.ok();
    }

    @GetMapping("/contacts")
    public R<List<ContactView>> contacts(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Long deptId) {
        String pattern = keyword == null ? "%" : "%" + keyword + "%";
        return R.ok(jdbc.query("SELECT e.id employee_id, e.employee_no, e.name, "
                        + "e.dept_id, d.name dept_name, p.name position_name, "
                        + "e.mobile, e.email FROM hr_employee e "
                        + "LEFT JOIN org_dept d ON d.id = e.dept_id "
                        + "LEFT JOIN org_position p ON p.id = e.position_id "
                        + "WHERE e.employment_status <> 'LEFT' "
                        + "AND (e.name LIKE ? OR e.employee_no LIKE ?) "
                        + "AND (? IS NULL OR e.dept_id = ?) ORDER BY d.sort, e.name",
                new Object[]{pattern, pattern, deptId, deptId}, (result, rowNum) -> {
                    ContactView row = new ContactView();
                    row.setEmployeeId(result.getLong("employee_id"));
                    row.setEmployeeNo(result.getString("employee_no"));
                    row.setName(result.getString("name"));
                    row.setDeptId(result.getObject("dept_id", Long.class));
                    row.setDeptName(result.getString("dept_name"));
                    row.setPosition(result.getString("position_name"));
                    row.setMobile(result.getString("mobile"));
                    row.setEmail(result.getString("email"));
                    return row;
                }));
    }

    @PostMapping("/expenses")
    public R<?> expense(@Valid @RequestBody ExpenseRequest request) {
        return R.ok(collabService.submitExpense(CurrentUser.id(), request));
    }

    @PostMapping("/expenses/{id}/pay")
    public R<Void> payExpense(@PathVariable Long id) {
        OaExpense expense = expenseService.getById(id);
        if (expense != null && "APPROVED".equals(expense.getStatus())) {
            expense.setStatus("PAID");
            expenseService.updateById(expense);
        }
        return R.ok();
    }
}
