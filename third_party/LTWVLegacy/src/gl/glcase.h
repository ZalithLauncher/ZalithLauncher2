#ifndef _GL4ES_GLCASE_H_
#define _GL4ES_GLCASE_H_

#define GL_TYPE_CASE(name, var, magic, vtype, code)\
    case magic: {                                  \
        vtype *name = (vtype *)var;                \
        code                                       \
        break;                                     \
    }

#define GL_TYPE_CASE_ALT(name, var, name2, var2, magic, vtype, code) \
    case magic: {                                  \
        vtype *name = (vtype *)var;                \
        vtype *name2= (vtype *)var2;               \
        code                                       \
        break;                                     \
    }

#define GL_TYPE_CASE2(name, var, magic, vtype, code2, code) \
    case magic: {                                  \
        code2 {                                    \
          vtype *name = (vtype *)var;              \
          code                                     \
        }                                          \
        break;                                     \
    }

#define GL_TYPE_CASE_MAX(name, var, magic, vtype, code, max) \
    case magic: {                                  \
	    vtype *name = (vtype *)var;                \
		vtype maxv = max;						   \
	    code                                       \
	    break;                                     \
    }
	
#define GL_TYPE_SWITCH(name, var, type, code, extra)               \
    switch (type) {                                                \
        GL_TYPE_CASE(name, var, GL_DOUBLE, GLdouble, code)         \
        GL_TYPE_CASE(name, var, GL_FLOAT, GLfloat, code)           \
        GL_TYPE_CASE(name, var, GL_INT, GLint, code)               \
        GL_TYPE_CASE(name, var, GL_SHORT, GLshort, code)           \
        GL_TYPE_CASE(name, var, GL_BYTE, GLbyte, code)   \
        GL_TYPE_CASE(name, var, GL_UNSIGNED_BYTE, GLubyte, code)   \
        GL_TYPE_CASE(name, var, GL_UNSIGNED_INT, GLuint, code)     \
        GL_TYPE_CASE(name, var, GL_UNSIGNED_SHORT, GLushort, code) \
        extra                                                      \
    }

#define GL_TYPE_SWITCH_ALT(name, var, name2, var2, type, code, extra)               \
    switch (type) {                                                                 \
        GL_TYPE_CASE_ALT(name, var, name2, var2, GL_DOUBLE, GLdouble, code)         \
        GL_TYPE_CASE_ALT(name, var, name2, var2, GL_FLOAT, GLfloat, code)           \
        GL_TYPE_CASE_ALT(name, var, name2, var2, GL_INT, GLint, code)               \
        GL_TYPE_CASE_ALT(name, var, name2, var2, GL_SHORT, GLshort, code)           \
        GL_TYPE_CASE_ALT(name, var, name2, var2, GL_BYTE, GLbyte, code)             \
        GL_TYPE_CASE_ALT(name, var, name2, var2, GL_UNSIGNED_BYTE, GLubyte, code)   \
        GL_TYPE_CASE_ALT(name, var, name2, var2, GL_UNSIGNED_INT, GLuint, code)     \
        GL_TYPE_CASE_ALT(name, var, name2, var2, GL_UNSIGNED_SHORT, GLushort, code) \
        extra                                                                       \
    }

#define GL_TYPE_SWITCH2(name, var, type, code2, code, extra)       \
    switch (type) {                                                \
        GL_TYPE_CASE2(name, var, GL_DOUBLE, GLdouble, code2, code) \
        GL_TYPE_CASE2(name, var, GL_FLOAT, GLfloat, code2, code)   \
        GL_TYPE_CASE2(name, var, GL_INT, GLint, code2, code)       \
        GL_TYPE_CASE2(name, var, GL_SHORT, GLshort, code2, code)   \
        GL_TYPE_CASE2(name, var, GL_BYTE, GLbyte, code2, code)     \
        GL_TYPE_CASE2(name, var, GL_UNSIGNED_BYTE, GLubyte, code2, code)   \
        GL_TYPE_CASE2(name, var, GL_UNSIGNED_INT, GLuint, code2, code)     \
        GL_TYPE_CASE2(name, var, GL_UNSIGNED_SHORT, GLushort, code2, code) \
        extra                                                      \
    }

#define GL_TYPE_SWITCH_MAX(name, var, type, code, extra)               \
    switch (type) {                                                \
	    GL_TYPE_CASE_MAX(name, var, GL_DOUBLE, GLdouble, code, 1.0)         \
	    GL_TYPE_CASE_MAX(name, var, GL_FLOAT, GLfloat, code, 1.0f)           \
	    GL_TYPE_CASE_MAX(name, var, GL_INT, GLint, code, 2147483647l)               \
	    GL_TYPE_CASE_MAX(name, var, GL_SHORT, GLshort, code, 32767)           \
	    GL_TYPE_CASE_MAX(name, var, GL_BYTE, GLbyte, code, 127)   \
	    GL_TYPE_CASE_MAX(name, var, GL_UNSIGNED_BYTE, GLubyte, code, 255)   \
	    GL_TYPE_CASE_MAX(name, var, GL_UNSIGNED_INT, GLuint, code, 4294967295l)     \
	    GL_TYPE_CASE_MAX(name, var, GL_UNSIGNED_SHORT, GLushort, code, 65535) \
		extra                                                      \
    }

#endif // _GL4ES_GLCASE_H_