#ifndef _GL4ES_MATVEC_H_
#define _GL4ES_MATVEC_H_

#include <math.h>
#include "attributes.h"

float dot(const float *a, const float *b) FASTMATH;
float dot4(const float *a, const float *b) FASTMATH;
void cross3(const float *a, const float *b, float* c) FASTMATH;
void matrix_vector(const float *a, const float *b, float *c);
void vector_matrix(const float *a, const float *b, float *c);
void vector3_matrix(const float *a, const float *b, float *c);
void vector3_matrix3(const float *a, const float *b, float *c);
void vector3_matrix4(const float *a, const float *b, float *c);
void vector_normalize(float *a);
void vector4_normalize(float *a);
void vector4_mult(const float *a, const float *b, float *c);
void vector4_add(const float *a, const float *b, float *c);
void matrix_transpose(const float *a, float *b);
void matrix_inverse(const float *m, float *r);
void matrix_inverse3_transpose(const float *m, float *r); // upper3x3 of matrix4 -> inverse -> transposed mat3
void matrix_mul(const float *a, const float *b, float *c);
void set_identity(float* mat);
int is_identity(const float* mat);

#endif // _GL4ES_MATVEC_H_
