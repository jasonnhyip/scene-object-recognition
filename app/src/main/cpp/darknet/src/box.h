#ifndef BOX_H
#define BOX_H
#include "darknet.h"


typedef struct{
    float dx, dy, dw, dh;
} dbox;


typedef struct detection_with_class {
    detection det;
    // The most probable class id: the best class index in this->prob.
    // Is filled temporary when processing results, otherwise not initialized
    int best_class;
} detection_with_class;

float box_rmse(box a, box b);
dbox diou(box a, box b);
box decode_box(box b, box anchor);
box encode_box(box b, box anchor);

detection_with_class* get_actual_detections(detection *dets, int dets_num, float thresh, int* selected_detections_num);
#endif
