/* Automatically generated nanopb constant definitions */
/* Generated by nanopb-0.3.9.3 at Tue Mar 19 19:18:41 2019. */

#include "sensor.pb.h"

/* @@protoc_insertion_point(includes) */
#if PB_PROTO_HEADER_VERSION != 30
#error Regenerate this file with the current version of nanopb generator.
#endif



const pb_field_t SensorMessage_fields[4] = {
    PB_FIELD(  1, INT32   , SINGULAR, STATIC  , FIRST, SensorMessage, sensor_type, sensor_type, 0),
    PB_FIELD(  2, FLOAT   , REPEATED, STATIC  , OTHER, SensorMessage, data, sensor_type, 0),
    PB_FIELD(  3, INT64   , SINGULAR, STATIC  , OTHER, SensorMessage, timestamp, data, 0),
    PB_LAST_FIELD
};


/* @@protoc_insertion_point(eof) */