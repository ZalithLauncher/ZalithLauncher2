#include "matvec.h"

#include <string.h>

float FASTMATH dot(const float *a, const float *b) {
    return a[0]*b[0] + a[1]*b[1] + a[2]*b[2];
}

float FASTMATH dot4(const float *a, const float *b) {
#if defined(__ARM_NEON__) && !defined(__APPLE__)
    register float ret;
    asm volatile (
    "vld1.f32 {d0-d1}, [%1]        \n" //q0 = a(0..3)
    "vld1.f32 {d2-d3}, [%2]        \n" //q1 = b(0..3)
    "vmul.f32 q0, q0, q1           \n" //q0 = a(0)*b(0),a(1)*b(1),a(2)*b(2),a(3)*b(3)
    "vadd.f32 d0, d0, d1           \n" //d0 = a(0)*b(0)+a(2)*b(2),a(1)*b(1)+a(3)*b(3)
    "vpadd.f32 d0,d0               \n" //d0 = a(0)*b(0)+a(2)*b(2)+a(1)*b(1)+a(3)*b(3),a(0)*b(0)+a(2)*b(2)+a(1)*b(1)+a(3)*b(3)
    "vmov.f32 %0, s0               \n"
    :"=w"(ret): "r"(a), "r"(b)
    : "q0", "q1"
        );
    return ret;
#else
    return a[0]*b[0] + a[1]*b[1] + a[2]*b[2] + a[3]*b[3];
#endif
}

void cross3(const float *a, const float *b, float* c) {
    //TODO Neonize? Cross product doesn't seems NEON friendly, and this is not much used.
    c[0] = a[1]*b[2] - a[2]*b[1];
    c[1] = a[3]*b[0] - a[0]*b[3];
    c[2] = a[0]*b[1] - a[1]*b[0];
}

void matrix_vector(const float *a, const float *b, float *c) {
#if defined(__ARM_NEON__) && !defined(__APPLE__)
    const float* a1 = a+8;
    asm volatile (
    "vld4.f32 {d0,d2,d4,d6}, [%1]        \n" 
    "vld4.f32 {d1,d3,d5,d7}, [%2]        \n" // q0-q3 = a(0,4,8,12/1,5,9,13/2,6,10,14/3,7,11,15)
    "vld1.f32 {q4}, [%3]       \n" // q4 = b
    "vmul.f32 q0, q0, d8[0]    \n" // q0 = a(0,4,8,12)*b[0]
    "vmla.f32 q0, q1, d8[1]    \n" // q0 = q0 + a(1,5,9,13)*b[1]
    "vmla.f32 q0, q2, d9[0]    \n" // q0 = q0 + a(2,6,10,14)*b[2]
    "vmla.f32 q0, q3, d9[1]    \n" // q0 = q0 + a(3,7,11,15)*b[3]
    "vst1.f32 {q0}, [%0]       \n"
    ::"r"(c), "r"(a), "r"(a1), "r"(b)
    : "q0", "q1", "q2", "q3", "q4", "memory"
        );
#else
    c[0] = a[0] * b[0] + a[1] * b[1] + a[2] * b[2] + a[3] * b[3];
    c[1] = a[4] * b[0] + a[5] * b[1] + a[6] * b[2] + a[7] * b[3];
    c[2] = a[8] * b[0] + a[9] * b[1] + a[10] * b[2] + a[11] * b[3];
    c[3] = a[12] * b[0] + a[13] * b[1] + a[14] * b[2] + a[15] * b[3];
#endif
}

void vector_matrix(const float *a, const float *b, float *c) {
#if defined(__ARM_NEON__) && !defined(__APPLE__)
    const float* b2=b+4;
    const float* b3=b+8;
    const float* b4=b+12;
    asm volatile (
    "vld1.f32 {q0}, [%1]        \n" // %q0 = a(0..3)
    "vld1.f32 {q1}, [%2]        \n" // %q1 = b(0..3)
    "vmul.f32 q1, q1, d0[0]     \n" // %q1 = b(0..3)*a[0]
    "vld1.f32 {q2}, [%3]        \n" // %q2 = b(4..7)
    "vmla.f32 q1, q2, d0[1]     \n" // %q1 = %q1 + b(4..7)*a[1]
    "vld1.f32 {q2}, [%4]        \n" // %q2 = b(8..11)
    "vmla.f32 q1, q2, d1[0]     \n" // %q1 = %q1 + b(8..11)*a[2]
    "vld1.f32 {q2}, [%5]        \n" // %q2 = b(12..15)
    "vmla.f32 q1, q2, d1[1]     \n" // %q1 = %q1 + b(12..15)*a[3]
    "vst1.f32 {q1}, [%0]        \n"
    ::"r"(c), "r"(a), "r"(b), "r"(b2), "r"(b3), "r"(b4)
    : "%2", "q0", "q1", "q2", "memory"
        );
#else
    const float a0=a[0], a1=a[1], a2=a[2], a3=a[3];
    c[0] = a0 * b[0] + a1 * b[4] + a2 * b[8] + a3 * b[12];
    c[1] = a0 * b[1] + a1 * b[5] + a2 * b[9] + a3 * b[13];
    c[2] = a0 * b[2] + a1 * b[6] + a2 * b[10] + a3 * b[14];
    c[3] = a0 * b[3] + a1 * b[7] + a2 * b[11] + a3 * b[15];
#endif
}

void vector3_matrix(const float *a, const float *b, float *c) {
#if defined(__ARM_NEON__) && !defined(__APPLE__)
    const float* b2=b+4;
    const float* b3=b+8;
    const float* b4=b+12;
    asm volatile (
    //"vld1.f32 {q0}, [%1]        \n" // %q0 = a(0..2)
    "vld1.32  {d0}, [%1]        \n"
    "flds     s2, [%1, #8]      \n"
    "vsub.f32 s3, s3, s3        \n"
    "vld1.f32 {q1}, [%2]        \n" // %q1 = b(0..3)
    "vmul.f32 q1, q1, d0[0]    \n" // %q1 = b(0..3)*a[0]
    "vld1.f32 {q2}, [%3]   \n" // %q2 = b(4..7)
    "vmla.f32 q1, q2, d0[1]    \n" // %q1 = %q1 + b(4..7)*a[1]
    "vld1.f32 {q2}, [%4]   \n" // %q2 = b(8..11)
    "vmla.f32 q1, q2, d1[0]    \n" // %q1 = %q1 + b(8..11)*a[2]
    "vld1.f32 {q2}, [%5]   \n" // %q2 = b(12..15)
    "vadd.f32 q1, q1, q2    \n" // %q1 = %q1 + b(12..15)
    "vst1.f32 {q1}, [%0]        \n"
    ::"r"(c), "r"(a), "r"(b), "r"(b2), "r"(b3), "r"(b4)
    : "q0", "q1", "q2", "memory"
        );
#else
    c[0] = a[0] * b[0] + a[1] * b[4] + a[2] * b[8] + b[12];
    c[1] = a[0] * b[1] + a[1] * b[5] + a[2] * b[9] + b[13];
    c[2] = a[0] * b[2] + a[1] * b[6] + a[2] * b[10] + b[14];
    c[3] = a[0] * b[3] + a[1] * b[7] + a[2] * b[11] + b[15];
#endif
}

void vector3_matrix4(const float *a, const float *b, float *c) {
    c[0] = a[0] * b[0] + a[1] * b[4] + a[2] * b[8];
    c[1] = a[0] * b[1] + a[1] * b[5] + a[2] * b[9];
    c[2] = a[0] * b[2] + a[1] * b[6] + a[2] * b[10];
}

void vector3_matrix3(const float *a, const float *b, float *c) {
    c[0] = a[0] * b[0] + a[1] * b[3] + a[2] * b[6];
    c[1] = a[0] * b[1] + a[1] * b[4] + a[2] * b[7];
    c[2] = a[0] * b[2] + a[1] * b[5] + a[2] * b[8];
}

void vector_normalize(float *a) {
#if defined(__ARM_NEON__) && !defined(__APPLE__)
        asm volatile (
        "vld1.32                {d4}, [%0]                      \n\t"   //d4={x0,y0}
        "flds                   s10, [%0, #8]                   \n\t"   //d5[0]={z0}
        "vsub.f32               s11, s11, s11                   \n\t"

        "vmul.f32               d0, d4, d4                      \n\t"   //d0= d4*d4
        "vpadd.f32              d0, d0                          \n\t"   //d0 = d[0] + d[1]
        "vmla.f32               d0, d5, d5                      \n\t"   //d0 = d0 + d5*d5 
        
        "vmov.f32               d1, d0                          \n\t"   //d1 = d0
        "vrsqrte.f32    		d0, d0                          \n\t"   //d0 = ~ 1.0 / sqrt(d0)
        "vmul.f32               d2, d0, d1                      \n\t"   //d2 = d0 * d1
        "vrsqrts.f32    		d3, d2, d0                      \n\t"   //d3 = (3 - d0 * d2) / 2        
        "vmul.f32               d0, d0, d3                      \n\t"   //d0 = d0 * d3
/*        "vmul.f32               d2, d0, d1                      \n\t"   //d2 = d0 * d1  
        "vrsqrts.f32    		d3, d2, d0                      \n\t"   //d4 = (3 - d0 * d3) / 2        
        "vmul.f32               d0, d0, d3                      \n\t"   //d0 = d0 * d4  */  // 1 iteration should be enough

        "vmul.f32               q2, q2, d0[0]                   \n\t"   //d0= d2*d4
        "vst1.32                {d4}, [%0]                     	\n\t"   //
        "fsts                   s10, [%0, #8]                   \n\t"   //
        
        :"+&r"(a): 
    : "d0", "d1", "d2", "d3", "d4", "d5", "memory"
        );
#else
    float det=1.0f/sqrtf(a[0]*a[0]+a[1]*a[1]+a[2]*a[2]);
    a[0]*=det;
    a[1]*=det;
    a[2]*=det;
#endif
}

void vector4_normalize(float *a) {
#if defined(__ARM_NEON__) && !defined(__APPLE__)
        asm volatile (
        "vld1.32                {q2}, [%0]                      \n\t"   //q2={x0,y0,z0,00}

        "vmul.f32               d0, d4, d4                      \n\t"   //d0= d4*d4
        "vpadd.f32              d0, d0                          \n\t"   //d0 = d[0] + d[1]
        "vmla.f32               d0, d5, d5                      \n\t"   //d0 = d0 + d5*d5 
        
        "vmov.f32               d1, d0                          \n\t"   //d1 = d0
        "vrsqrte.f32    		d0, d0                          \n\t"   //d0 = ~ 1.0 / sqrt(d0)
        "vmul.f32               d2, d0, d1                      \n\t"   //d2 = d0 * d1
        "vrsqrts.f32    		d3, d2, d0                      \n\t"   //d3 = (3 - d0 * d2) / 2        
        "vmul.f32               d0, d0, d3                      \n\t"   //d0 = d0 * d3
/*        "vmul.f32               d2, d0, d1                      \n\t"   //d2 = d0 * d1  
        "vrsqrts.f32    		d3, d2, d0                      \n\t"   //d4 = (3 - d0 * d3) / 2        
        "vmul.f32               d0, d0, d3                      \n\t"   //d0 = d0 * d4  */  // 1 iteration should be enough

        "vmul.f32               q2, q2, d0[0]                   \n\t"   //d0= d2*d4
        "vst1.32                {q2}, [%0]                    	\n\t"   //
        
        :"+&r"(a): 
    : "d0", "d1", "d2", "d3", "d4", "d5", "memory"
        );
#else
    float det=1.0f/sqrtf(a[0]*a[0]+a[1]*a[1]+a[2]*a[2]);
    a[0]*=det;
    a[1]*=det;
    a[2]*=det;
    // a[3] is ignored and left as 0.0f
#endif
}

void FASTMATH matrix_transpose(const float *a, float *b) {
    // column major -> row major
    // a(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15) -> b(0,4,8,12,1,5,9,13,2,6,10,14,3,7,11,15)
#if defined(__ARM_NEON__) && !defined(__APPLE__)
   const float* a1 = a+8;
	float* b1=b+8;
    asm volatile (
    "vld4.f32 {d0,d2,d4,d6}, [%1]        \n" 
    "vld4.f32 {d1,d3,d5,d7}, [%2]        \n" // %q0-%q3 = a(0,4,8,12/1,5,9,13/2,6,10,14/3,7,11,15)
    "vst1.f32 {d0-d3}, [%0]        \n"
    "vst1.f32 {d4-d7}, [%3]        \n"
    ::"r"(b), "r"(a), "r"(a1), "r"(b1)
    : "q0", "q1", "q2", "q3", "memory"
        );
#else
    for (int i=0; i<4; i++)
        for (int j=0; j<4; j++)
            b[i*4+j]=a[i+j*4];
#endif
}

void matrix_inverse(const float *m, float *r) {

    r[0] = m[5]*m[10]*m[15] - m[5]*m[14]*m[11] - m[6]*m[9]*m[15] + m[6]*m[13]*m[11] + m[7]*m[9]*m[14] - m[7]*m[13]*m[10];
    r[1] = -m[1]*m[10]*m[15] + m[1]*m[14]*m[11] + m[2]*m[9]*m[15] - m[2]*m[13]*m[11] - m[3]*m[9]*m[14] + m[3]*m[13]*m[10];
    r[2] = m[1]*m[6]*m[15] - m[1]*m[14]*m[7] - m[2]*m[5]*m[15] + m[2]*m[13]*m[7] + m[3]*m[5]*m[14] - m[3]*m[13]*m[6];
    r[3] = -m[1]*m[6]*m[11] + m[1]*m[10]*m[7] + m[2]*m[5]*m[11] - m[2]*m[9]*m[7] - m[3]*m[5]*m[10] + m[3]*m[9]*m[6];

    r[4] = -m[4]*m[10]*m[15] + m[4]*m[14]*m[11] + m[6]*m[8]*m[15] - m[6]*m[12]*m[11] - m[7]*m[8]*m[14] + m[7]*m[12]*m[10];
    r[5] = m[0]*m[10]*m[15] - m[0]*m[14]*m[11] - m[2]*m[8]*m[15] + m[2]*m[12]*m[11] + m[3]*m[8]*m[14] - m[3]*m[12]*m[10];
    r[6] = -m[0]*m[6]*m[15] + m[0]*m[14]*m[7] + m[2]*m[4]*m[15] - m[2]*m[12]*m[7] - m[3]*m[4]*m[14] + m[3]*m[12]*m[6];
    r[7] = m[0]*m[6]*m[11] - m[0]*m[10]*m[7] - m[2]*m[4]*m[11] + m[2]*m[8]*m[7] + m[3]*m[4]*m[10] - m[3]*m[8]*m[6];

    r[8] = m[4]*m[9]*m[15] - m[4]*m[13]*m[11] - m[5]*m[8]*m[15] + m[5]*m[12]*m[11] + m[7]*m[8]*m[13] - m[7]*m[12]*m[9];
    r[9] = -m[0]*m[9]*m[15] + m[0]*m[13]*m[11] + m[1]*m[8]*m[15] - m[1]*m[12]*m[11] - m[3]*m[8]*m[13] + m[3]*m[12]*m[9];
    r[10] = m[0]*m[5]*m[15] - m[0]*m[13]*m[7] - m[1]*m[4]*m[15] + m[1]*m[12]*m[7] + m[3]*m[4]*m[13] - m[3]*m[12]*m[5];
    r[11] = -m[0]*m[5]*m[11] + m[0]*m[9]*m[7] + m[1]*m[4]*m[11] - m[1]*m[8]*m[7] - m[3]*m[4]*m[9] + m[3]*m[8]*m[5];

    r[12] = -m[4]*m[9]*m[14] + m[4]*m[13]*m[10] + m[5]*m[8]*m[14] - m[5]*m[12]*m[10] - m[6]*m[8]*m[13] + m[6]*m[12]*m[9];
    r[13] = m[0]*m[9]*m[14] - m[0]*m[13]*m[10] - m[1]*m[8]*m[14] + m[1]*m[12]*m[10] + m[2]*m[8]*m[13] - m[2]*m[12]*m[9];
    r[14] = -m[0]*m[5]*m[14] + m[0]*m[13]*m[6] + m[1]*m[4]*m[14] - m[1]*m[12]*m[6] - m[2]*m[4]*m[13] + m[2]*m[12]*m[5];
    r[15] = m[0]*m[5]*m[10] - m[0]*m[9]*m[6] - m[1]*m[4]*m[10] + m[1]*m[8]*m[6] + m[2]*m[4]*m[9] - m[2]*m[8]*m[5];

    float det = 1.0f/(m[0]*r[0] + m[1]*r[4] + m[2]*r[8] + m[3]*r[12]);
    for (int i = 0; i < 16; i++) r[i] *= det;
}

void matrix_inverse3_transpose(const float *m, float *r) {
    
    r[0] = m[4+1]*m[8+2] - m[4+2]*m[8+1];
    r[1] = m[4+2]*m[8+0] - m[4+0]*m[8+2];
    r[2] = m[4+0]*m[8+1] - m[4+1]*m[8+0];

    r[3] = m[0+2]*m[8+1] - m[0+1]*m[8+2];
    r[4] = m[0+0]*m[8+2] - m[0+2]*m[8+0];
    r[5] = m[0+1]*m[8+0] - m[0+0]*m[8+1];

    r[6] = m[0+1]*m[4+2] - m[0+2]*m[4+1];
    r[7] = m[0+2]*m[4+0] - m[0+0]*m[4+2];
    r[8] = m[0+0]*m[4+1] - m[0+1]*m[4+0];

    float det = 1.0f/(m[0]*r[0] + m[4+0]*r[3] + m[8+0]*r[6]);
    for (int i = 0; i < 9; i++) r[i] *= det;
}
    
void matrix_mul(const float *a, const float *b, float *c) {
#if defined(__ARM_NEON__) && !defined(__APPLE__)
    const float* a1 = a+8;
	const float* b1=b+8;
    float* c1=c+8;
    asm volatile (
    "vld1.32  {d16-d19}, [%2]       \n" 
    "vld1.32  {d20-d23}, [%3]       \n"
    "vld1.32  {d0-d3}, [%4]         \n"
    "vld1.32  {d4-d7}, [%5]         \n"
    "vmul.f32 q12, q8, d0[0]        \n"
    "vmul.f32 q13, q8, d2[0]        \n"
    "vmul.f32 q14, q8, d4[0]        \n"
    "vmul.f32 q15, q8, d6[0]        \n"
    "vmla.f32 q12, q9, d0[1]        \n"
    "vmla.f32 q13, q9, d2[1]        \n"
    "vmla.f32 q14, q9, d4[1]        \n"
    "vmla.f32 q15, q9, d6[1]        \n"
    "vmla.f32 q12, q10, d1[0]       \n"
    "vmla.f32 q13, q10, d3[0]       \n"
    "vmla.f32 q14, q10, d5[0]       \n"
    "vmla.f32 q15, q10, d7[0]       \n"
    "vmla.f32 q12, q11, d1[1]       \n"
    "vmla.f32 q13, q11, d3[1]       \n"
    "vmla.f32 q14, q11, d5[1]       \n"
    "vmla.f32 q15, q11, d7[1]       \n"
    "vst1.32  {d24-d27}, [%0]       \n"
    "vst1.32  {d28-d31}, [%1]       \n"
    ::"r"(c), "r"(c1), "r"(a), "r"(a1), "r"(b), "r"(b1)
    : "q0", "q1", "q2", "q3", 
      "q8", "q9", "q10", "q11", "q12", "q13", "q14", "q15", "memory"
        );
#else
   float a00 = a[0], a01 = a[1], a02 = a[2], a03 = a[3],
        a10 = a[4], a11 = a[5], a12 = a[6], a13 = a[7],
        a20 = a[8], a21 = a[9], a22 = a[10], a23 = a[11],
        a30 = a[12], a31 = a[13], a32 = a[14], a33 = a[15];

    float b0  = b[0], b1 = b[1], b2 = b[2], b3 = b[3];
    c[0] = b0*a00 + b1*a10 + b2*a20 + b3*a30;
    c[1] = b0*a01 + b1*a11 + b2*a21 + b3*a31;
    c[2] = b0*a02 + b1*a12 + b2*a22 + b3*a32;
    c[3] = b0*a03 + b1*a13 + b2*a23 + b3*a33;

    b0 = b[4]; b1 = b[5]; b2 = b[6]; b3 = b[7];
    c[4] = b0*a00 + b1*a10 + b2*a20 + b3*a30;
    c[5] = b0*a01 + b1*a11 + b2*a21 + b3*a31;
    c[6] = b0*a02 + b1*a12 + b2*a22 + b3*a32;
    c[7] = b0*a03 + b1*a13 + b2*a23 + b3*a33;

    b0 = b[8]; b1 = b[9]; b2 = b[10]; b3 = b[11];
    c[8] = b0*a00 + b1*a10 + b2*a20 + b3*a30;
    c[9] = b0*a01 + b1*a11 + b2*a21 + b3*a31;
    c[10] = b0*a02 + b1*a12 + b2*a22 + b3*a32;
    c[11] = b0*a03 + b1*a13 + b2*a23 + b3*a33;

    b0 = b[12]; b1 = b[13]; b2 = b[14]; b3 = b[15];
    c[12] = b0*a00 + b1*a10 + b2*a20 + b3*a30;
    c[13] = b0*a01 + b1*a11 + b2*a21 + b3*a31;
    c[14] = b0*a02 + b1*a12 + b2*a22 + b3*a32;
    c[15] = b0*a03 + b1*a13 + b2*a23 + b3*a33;
#endif
}

void vector4_mult(const float *a, const float *b, float *c) {
//TODO: NEON version of this
    for (int i=0; i<4; i++)
        c[i] = a[i]*b[i];
}

void vector4_add(const float *a, const float *b, float *c) {
//TODO: NEON version of this
    for (int i=0; i<4; i++)
        c[i] = a[i]+b[i];
}

void vector4_sub(const float *a, const float *b, float *c) {
    //TODO: NEON version of this
        for (int i=0; i<4; i++)
            c[i] = a[i]-b[i];
}
    
void set_identity(float* mat) {
    memset(mat, 0, 16*sizeof(float));
    mat[0] = mat[1+4] = mat[2+8] = mat[3+12] = 1.0f;
}

int is_identity(const float* mat) {
    static float i1[16];
    static int set=0;
    if(!set) {set_identity(i1); set=1;}
    return memcmp(mat, i1, 16*sizeof(float))==0?1:0;
}
