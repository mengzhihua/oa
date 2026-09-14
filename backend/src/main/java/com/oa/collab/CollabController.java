package com.oa.collab;

import com.oa.collab.dto.ExpenseRequest;
import com.oa.collab.dto.MeetingBookingRequest;
import com.oa.collab.dto.NoticeRequest;
import com.oa.collab.entity.OaMeetingBooking;
import com.oa.collab.entity.OaMeetingRoom;
import com.oa.collab.entity.OaMessage;
import com.oa.collab.entity.OaNotice;
import com.oa.collab.entity.OaSchedule;
import com.oa.collab.service.CollabService;
import com.oa.collab.service.OaMeetingRoomService;
import com.oa.collab.service.OaMeetingBookingService;
import com.oa.collab.service.OaScheduleService;
import com.oa.common.R;
import com.oa.system.auth.CurrentUser;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    public CollabController(CollabService collabService,
                            OaMeetingRoomService roomService,
                            OaMeetingBookingService bookingService,
                            OaScheduleService scheduleService) {
        this.collabService = collabService;
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.scheduleService = scheduleService;
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

    @PostMapping("/expenses")
    public R<?> expense(@Valid @RequestBody ExpenseRequest request) {
        return R.ok(collabService.submitExpense(CurrentUser.id(), request));
    }
}
