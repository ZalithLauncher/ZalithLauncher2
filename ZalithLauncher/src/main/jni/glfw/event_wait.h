#ifndef GW_EVENT_WAIT_H
#define GW_EVENT_WAIT_H

#include <stdatomic.h>
#include <stdbool.h>
#include <stdint.h>

enum {
    GW_EVENT_READY = 0,
    GW_EVENT_TIMEOUT = -1,
    GW_EVENT_NOT_INITIALIZED = 0x10001,
    GW_EVENT_INVALID_VALUE = 0x10004,
    GW_EVENT_PLATFORM_ERROR = 0x10008
};

struct gw_event_wait {
    _Atomic uint32_t sequence;
    _Atomic uint32_t consumed;
    _Atomic bool active;
    uint32_t pumping;
};

void gw_events_reset(struct gw_event_wait *events);
void gw_events_stop(struct gw_event_wait *events);
int gw_events_post(struct gw_event_wait *events);
void gw_events_begin_pump(struct gw_event_wait *events);
void gw_events_end_pump(struct gw_event_wait *events);
int gw_events_wait(struct gw_event_wait *events, double seconds, bool timed);

#endif
