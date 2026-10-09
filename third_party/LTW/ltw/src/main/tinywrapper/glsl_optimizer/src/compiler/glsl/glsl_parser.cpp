/* A Bison parser, made by GNU Bison 3.8.2.  */

/* Bison implementation for Yacc-like parsers in C

   Copyright (C) 1984, 1989-1990, 2000-2015, 2018-2021 Free Software Foundation,
   Inc.

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <https://www.gnu.org/licenses/>.  */

/* As a special exception, you may create a larger work that contains
   part or all of the Bison parser skeleton and distribute that work
   under terms of your choice, so long as that work isn't itself a
   parser generator using the skeleton or a modified version thereof
   as a parser skeleton.  Alternatively, if you modify or redistribute
   the parser skeleton itself, you may (at your option) remove this
   special exception, which will cause the skeleton and the resulting
   Bison output files to be licensed under the GNU General Public
   License without this special exception.

   This special exception was added by the Free Software Foundation in
   version 2.2 of Bison.  */

/* C LALR(1) parser skeleton written by Richard Stallman, by
   simplifying the original so-called "semantic" parser.  */

/* DO NOT RELY ON FEATURES THAT ARE NOT DOCUMENTED in the manual,
   especially those whose name start with YY_ or yy_.  They are
   private implementation details that can be changed or removed.  */

/* All symbols defined below should begin with yy or YY, to avoid
   infringing on user name space.  This should be done even for local
   variables, as they might otherwise be expanded by user macros.
   There are some unavoidable exceptions within include files to
   define necessary library symbols; they are noted "INFRINGES ON
   USER NAME SPACE" below.  */

/* Identify Bison output, and Bison version.  */
#define YYBISON 30802

/* Bison version string.  */
#define YYBISON_VERSION "3.8.2"

/* Skeleton name.  */
#define YYSKELETON_NAME "yacc.c"

/* Pure parsers.  */
#define YYPURE 1

/* Push parsers.  */
#define YYPUSH 0

/* Pull parsers.  */
#define YYPULL 1


/* Substitute the variable and function names.  */
#define yyparse         _mesa_glsl_parse
#define yylex           _mesa_glsl_lex
#define yyerror         _mesa_glsl_error
#define yydebug         _mesa_glsl_debug
#define yynerrs         _mesa_glsl_nerrs

/* First part of user prologue.  */
#line 1 "../src/compiler/glsl/glsl_parser.yy"

/*
 * Copyright Â© 2008, 2009 Intel Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice (including the next
 * paragraph) shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#ifndef _MSC_VER
#include <strings.h>
#endif
#include <assert.h>

#include "ast.h"
#include "glsl_parser_extras.h"
#include "../glsl_types.h"
#include "../../util/u_string.h"
#include "../../util/format/u_format.h"
#include "../../mesa/main/consts_exts.h"

#ifdef _MSC_VER
#pragma warning( disable : 4065 ) // switch statement contains 'default' but no 'case' labels
#endif

#undef yyerror

static void yyerror(YYLTYPE *loc, _mesa_glsl_parse_state *st, const char *msg)
{
   _mesa_glsl_error(loc, st, "%s", msg);
}

static int
_mesa_glsl_lex(YYSTYPE *val, YYLTYPE *loc, _mesa_glsl_parse_state *state)
{
   return _mesa_glsl_lexer_lex(val, loc, state->scanner);
}

static bool match_layout_qualifier(const char *s1, const char *s2,
                                   _mesa_glsl_parse_state *state)
{
   /* From the GLSL 1.50 spec, section 4.3.8 (Layout Qualifiers):
    *
    *     "The tokens in any layout-qualifier-id-list ... are not case
    *     sensitive, unless explicitly noted otherwise."
    *
    * The text "unless explicitly noted otherwise" appears to be
    * vacuous--no desktop GLSL spec (up through GLSL 4.40) notes
    * otherwise.
    *
    * However, the GLSL ES 3.00 spec says, in section 4.3.8 (Layout
    * Qualifiers):
    *
    *     "As for other identifiers, they are case sensitive."
    *
    * So we need to do a case-sensitive or a case-insensitive match,
    * depending on whether we are compiling for GLSL ES.
    */
   if (state->es_shader)
      return strcmp(s1, s2);
   else
      return strcasecmp(s1, s2);
}

#line 158 "src/compiler/glsl/glsl_parser.cpp"

# ifndef YY_CAST
#  ifdef __cplusplus
#   define YY_CAST(Type, Val) static_cast<Type> (Val)
#   define YY_REINTERPRET_CAST(Type, Val) reinterpret_cast<Type> (Val)
#  else
#   define YY_CAST(Type, Val) ((Type) (Val))
#   define YY_REINTERPRET_CAST(Type, Val) ((Type) (Val))
#  endif
# endif
# ifndef YY_NULLPTR
#  if defined __cplusplus
#   if 201103L <= __cplusplus
#    define YY_NULLPTR nullptr
#   else
#    define YY_NULLPTR 0
#   endif
#  else
#   define YY_NULLPTR ((void*)0)
#  endif
# endif

#include "glsl_parser.h"
/* Symbol kind.  */
enum yysymbol_kind_t
{
  YYSYMBOL_YYEMPTY = -2,
  YYSYMBOL_YYEOF = 0,                      /* "end of file"  */
  YYSYMBOL_YYerror = 1,                    /* error  */
  YYSYMBOL_YYUNDEF = 2,                    /* "invalid token"  */
  YYSYMBOL_ATTRIBUTE = 3,                  /* ATTRIBUTE  */
  YYSYMBOL_CONST_TOK = 4,                  /* CONST_TOK  */
  YYSYMBOL_BASIC_TYPE_TOK = 5,             /* BASIC_TYPE_TOK  */
  YYSYMBOL_BREAK = 6,                      /* BREAK  */
  YYSYMBOL_BUFFER = 7,                     /* BUFFER  */
  YYSYMBOL_CONTINUE = 8,                   /* CONTINUE  */
  YYSYMBOL_DO = 9,                         /* DO  */
  YYSYMBOL_ELSE = 10,                      /* ELSE  */
  YYSYMBOL_FOR = 11,                       /* FOR  */
  YYSYMBOL_IF = 12,                        /* IF  */
  YYSYMBOL_DEMOTE = 13,                    /* DEMOTE  */
  YYSYMBOL_DISCARD = 14,                   /* DISCARD  */
  YYSYMBOL_RETURN = 15,                    /* RETURN  */
  YYSYMBOL_SWITCH = 16,                    /* SWITCH  */
  YYSYMBOL_CASE = 17,                      /* CASE  */
  YYSYMBOL_DEFAULT = 18,                   /* DEFAULT  */
  YYSYMBOL_CENTROID = 19,                  /* CENTROID  */
  YYSYMBOL_IN_TOK = 20,                    /* IN_TOK  */
  YYSYMBOL_OUT_TOK = 21,                   /* OUT_TOK  */
  YYSYMBOL_INOUT_TOK = 22,                 /* INOUT_TOK  */
  YYSYMBOL_UNIFORM = 23,                   /* UNIFORM  */
  YYSYMBOL_VARYING = 24,                   /* VARYING  */
  YYSYMBOL_SAMPLE = 25,                    /* SAMPLE  */
  YYSYMBOL_NOPERSPECTIVE = 26,             /* NOPERSPECTIVE  */
  YYSYMBOL_FLAT = 27,                      /* FLAT  */
  YYSYMBOL_SMOOTH = 28,                    /* SMOOTH  */
  YYSYMBOL_IMAGE1DSHADOW = 29,             /* IMAGE1DSHADOW  */
  YYSYMBOL_IMAGE2DSHADOW = 30,             /* IMAGE2DSHADOW  */
  YYSYMBOL_IMAGE1DARRAYSHADOW = 31,        /* IMAGE1DARRAYSHADOW  */
  YYSYMBOL_IMAGE2DARRAYSHADOW = 32,        /* IMAGE2DARRAYSHADOW  */
  YYSYMBOL_COHERENT = 33,                  /* COHERENT  */
  YYSYMBOL_VOLATILE = 34,                  /* VOLATILE  */
  YYSYMBOL_RESTRICT = 35,                  /* RESTRICT  */
  YYSYMBOL_READONLY = 36,                  /* READONLY  */
  YYSYMBOL_WRITEONLY = 37,                 /* WRITEONLY  */
  YYSYMBOL_SHARED = 38,                    /* SHARED  */
  YYSYMBOL_STRUCT = 39,                    /* STRUCT  */
  YYSYMBOL_VOID_TOK = 40,                  /* VOID_TOK  */
  YYSYMBOL_WHILE = 41,                     /* WHILE  */
  YYSYMBOL_IDENTIFIER = 42,                /* IDENTIFIER  */
  YYSYMBOL_TYPE_IDENTIFIER = 43,           /* TYPE_IDENTIFIER  */
  YYSYMBOL_NEW_IDENTIFIER = 44,            /* NEW_IDENTIFIER  */
  YYSYMBOL_FLOATCONSTANT = 45,             /* FLOATCONSTANT  */
  YYSYMBOL_DOUBLECONSTANT = 46,            /* DOUBLECONSTANT  */
  YYSYMBOL_INTCONSTANT = 47,               /* INTCONSTANT  */
  YYSYMBOL_UINTCONSTANT = 48,              /* UINTCONSTANT  */
  YYSYMBOL_BOOLCONSTANT = 49,              /* BOOLCONSTANT  */
  YYSYMBOL_INT64CONSTANT = 50,             /* INT64CONSTANT  */
  YYSYMBOL_UINT64CONSTANT = 51,            /* UINT64CONSTANT  */
  YYSYMBOL_FIELD_SELECTION = 52,           /* FIELD_SELECTION  */
  YYSYMBOL_LEFT_OP = 53,                   /* LEFT_OP  */
  YYSYMBOL_RIGHT_OP = 54,                  /* RIGHT_OP  */
  YYSYMBOL_INC_OP = 55,                    /* INC_OP  */
  YYSYMBOL_DEC_OP = 56,                    /* DEC_OP  */
  YYSYMBOL_LE_OP = 57,                     /* LE_OP  */
  YYSYMBOL_GE_OP = 58,                     /* GE_OP  */
  YYSYMBOL_EQ_OP = 59,                     /* EQ_OP  */
  YYSYMBOL_NE_OP = 60,                     /* NE_OP  */
  YYSYMBOL_AND_OP = 61,                    /* AND_OP  */
  YYSYMBOL_OR_OP = 62,                     /* OR_OP  */
  YYSYMBOL_XOR_OP = 63,                    /* XOR_OP  */
  YYSYMBOL_MUL_ASSIGN = 64,                /* MUL_ASSIGN  */
  YYSYMBOL_DIV_ASSIGN = 65,                /* DIV_ASSIGN  */
  YYSYMBOL_ADD_ASSIGN = 66,                /* ADD_ASSIGN  */
  YYSYMBOL_MOD_ASSIGN = 67,                /* MOD_ASSIGN  */
  YYSYMBOL_LEFT_ASSIGN = 68,               /* LEFT_ASSIGN  */
  YYSYMBOL_RIGHT_ASSIGN = 69,              /* RIGHT_ASSIGN  */
  YYSYMBOL_AND_ASSIGN = 70,                /* AND_ASSIGN  */
  YYSYMBOL_XOR_ASSIGN = 71,                /* XOR_ASSIGN  */
  YYSYMBOL_OR_ASSIGN = 72,                 /* OR_ASSIGN  */
  YYSYMBOL_SUB_ASSIGN = 73,                /* SUB_ASSIGN  */
  YYSYMBOL_INVARIANT = 74,                 /* INVARIANT  */
  YYSYMBOL_PRECISE = 75,                   /* PRECISE  */
  YYSYMBOL_LOWP = 76,                      /* LOWP  */
  YYSYMBOL_MEDIUMP = 77,                   /* MEDIUMP  */
  YYSYMBOL_HIGHP = 78,                     /* HIGHP  */
  YYSYMBOL_SUPERP = 79,                    /* SUPERP  */
  YYSYMBOL_PRECISION = 80,                 /* PRECISION  */
  YYSYMBOL_VERSION_TOK = 81,               /* VERSION_TOK  */
  YYSYMBOL_EXTENSION = 82,                 /* EXTENSION  */
  YYSYMBOL_LINE = 83,                      /* LINE  */
  YYSYMBOL_COLON = 84,                     /* COLON  */
  YYSYMBOL_EOL = 85,                       /* EOL  */
  YYSYMBOL_INTERFACE_TOK = 86,             /* INTERFACE_TOK  */
  YYSYMBOL_OUTPUT = 87,                    /* OUTPUT  */
  YYSYMBOL_PRAGMA_DEBUG_ON = 88,           /* PRAGMA_DEBUG_ON  */
  YYSYMBOL_PRAGMA_DEBUG_OFF = 89,          /* PRAGMA_DEBUG_OFF  */
  YYSYMBOL_PRAGMA_OPTIMIZE_ON = 90,        /* PRAGMA_OPTIMIZE_ON  */
  YYSYMBOL_PRAGMA_OPTIMIZE_OFF = 91,       /* PRAGMA_OPTIMIZE_OFF  */
  YYSYMBOL_PRAGMA_WARNING_ON = 92,         /* PRAGMA_WARNING_ON  */
  YYSYMBOL_PRAGMA_WARNING_OFF = 93,        /* PRAGMA_WARNING_OFF  */
  YYSYMBOL_PRAGMA_INVARIANT_ALL = 94,      /* PRAGMA_INVARIANT_ALL  */
  YYSYMBOL_LAYOUT_TOK = 95,                /* LAYOUT_TOK  */
  YYSYMBOL_DOT_TOK = 96,                   /* DOT_TOK  */
  YYSYMBOL_ASM = 97,                       /* ASM  */
  YYSYMBOL_CLASS = 98,                     /* CLASS  */
  YYSYMBOL_UNION = 99,                     /* UNION  */
  YYSYMBOL_ENUM = 100,                     /* ENUM  */
  YYSYMBOL_TYPEDEF = 101,                  /* TYPEDEF  */
  YYSYMBOL_TEMPLATE = 102,                 /* TEMPLATE  */
  YYSYMBOL_THIS = 103,                     /* THIS  */
  YYSYMBOL_PACKED_TOK = 104,               /* PACKED_TOK  */
  YYSYMBOL_GOTO = 105,                     /* GOTO  */
  YYSYMBOL_INLINE_TOK = 106,               /* INLINE_TOK  */
  YYSYMBOL_NOINLINE = 107,                 /* NOINLINE  */
  YYSYMBOL_PUBLIC_TOK = 108,               /* PUBLIC_TOK  */
  YYSYMBOL_STATIC = 109,                   /* STATIC  */
  YYSYMBOL_EXTERN = 110,                   /* EXTERN  */
  YYSYMBOL_EXTERNAL = 111,                 /* EXTERNAL  */
  YYSYMBOL_LONG_TOK = 112,                 /* LONG_TOK  */
  YYSYMBOL_SHORT_TOK = 113,                /* SHORT_TOK  */
  YYSYMBOL_HALF = 114,                     /* HALF  */
  YYSYMBOL_FIXED_TOK = 115,                /* FIXED_TOK  */
  YYSYMBOL_UNSIGNED = 116,                 /* UNSIGNED  */
  YYSYMBOL_INPUT_TOK = 117,                /* INPUT_TOK  */
  YYSYMBOL_HVEC2 = 118,                    /* HVEC2  */
  YYSYMBOL_HVEC3 = 119,                    /* HVEC3  */
  YYSYMBOL_HVEC4 = 120,                    /* HVEC4  */
  YYSYMBOL_FVEC2 = 121,                    /* FVEC2  */
  YYSYMBOL_FVEC3 = 122,                    /* FVEC3  */
  YYSYMBOL_FVEC4 = 123,                    /* FVEC4  */
  YYSYMBOL_SAMPLER3DRECT = 124,            /* SAMPLER3DRECT  */
  YYSYMBOL_SIZEOF = 125,                   /* SIZEOF  */
  YYSYMBOL_CAST = 126,                     /* CAST  */
  YYSYMBOL_NAMESPACE = 127,                /* NAMESPACE  */
  YYSYMBOL_USING = 128,                    /* USING  */
  YYSYMBOL_RESOURCE = 129,                 /* RESOURCE  */
  YYSYMBOL_PATCH = 130,                    /* PATCH  */
  YYSYMBOL_SUBROUTINE = 131,               /* SUBROUTINE  */
  YYSYMBOL_ERROR_TOK = 132,                /* ERROR_TOK  */
  YYSYMBOL_COMMON = 133,                   /* COMMON  */
  YYSYMBOL_PARTITION = 134,                /* PARTITION  */
  YYSYMBOL_ACTIVE = 135,                   /* ACTIVE  */
  YYSYMBOL_FILTER = 136,                   /* FILTER  */
  YYSYMBOL_ROW_MAJOR = 137,                /* ROW_MAJOR  */
  YYSYMBOL_THEN = 138,                     /* THEN  */
  YYSYMBOL_139_ = 139,                     /* '('  */
  YYSYMBOL_140_ = 140,                     /* ')'  */
  YYSYMBOL_141_ = 141,                     /* '['  */
  YYSYMBOL_142_ = 142,                     /* ']'  */
  YYSYMBOL_143_ = 143,                     /* ','  */
  YYSYMBOL_144_ = 144,                     /* '+'  */
  YYSYMBOL_145_ = 145,                     /* '-'  */
  YYSYMBOL_146_ = 146,                     /* '!'  */
  YYSYMBOL_147_ = 147,                     /* '~'  */
  YYSYMBOL_148_ = 148,                     /* '*'  */
  YYSYMBOL_149_ = 149,                     /* '/'  */
  YYSYMBOL_150_ = 150,                     /* '%'  */
  YYSYMBOL_151_ = 151,                     /* '<'  */
  YYSYMBOL_152_ = 152,                     /* '>'  */
  YYSYMBOL_153_ = 153,                     /* '&'  */
  YYSYMBOL_154_ = 154,                     /* '^'  */
  YYSYMBOL_155_ = 155,                     /* '|'  */
  YYSYMBOL_156_ = 156,                     /* '?'  */
  YYSYMBOL_157_ = 157,                     /* ':'  */
  YYSYMBOL_158_ = 158,                     /* '='  */
  YYSYMBOL_159_ = 159,                     /* ';'  */
  YYSYMBOL_160_ = 160,                     /* '{'  */
  YYSYMBOL_161_ = 161,                     /* '}'  */
  YYSYMBOL_YYACCEPT = 162,                 /* $accept  */
  YYSYMBOL_translation_unit = 163,         /* translation_unit  */
  YYSYMBOL_164_1 = 164,                    /* $@1  */
  YYSYMBOL_version_statement = 165,        /* version_statement  */
  YYSYMBOL_pragma_statement = 166,         /* pragma_statement  */
  YYSYMBOL_extension_statement_list = 167, /* extension_statement_list  */
  YYSYMBOL_any_identifier = 168,           /* any_identifier  */
  YYSYMBOL_extension_statement = 169,      /* extension_statement  */
  YYSYMBOL_external_declaration_list = 170, /* external_declaration_list  */
  YYSYMBOL_variable_identifier = 171,      /* variable_identifier  */
  YYSYMBOL_primary_expression = 172,       /* primary_expression  */
  YYSYMBOL_postfix_expression = 173,       /* postfix_expression  */
  YYSYMBOL_integer_expression = 174,       /* integer_expression  */
  YYSYMBOL_function_call = 175,            /* function_call  */
  YYSYMBOL_function_call_or_method = 176,  /* function_call_or_method  */
  YYSYMBOL_function_call_generic = 177,    /* function_call_generic  */
  YYSYMBOL_function_call_header_no_parameters = 178, /* function_call_header_no_parameters  */
  YYSYMBOL_function_call_header_with_parameters = 179, /* function_call_header_with_parameters  */
  YYSYMBOL_function_call_header = 180,     /* function_call_header  */
  YYSYMBOL_function_identifier = 181,      /* function_identifier  */
  YYSYMBOL_unary_expression = 182,         /* unary_expression  */
  YYSYMBOL_unary_operator = 183,           /* unary_operator  */
  YYSYMBOL_multiplicative_expression = 184, /* multiplicative_expression  */
  YYSYMBOL_additive_expression = 185,      /* additive_expression  */
  YYSYMBOL_shift_expression = 186,         /* shift_expression  */
  YYSYMBOL_relational_expression = 187,    /* relational_expression  */
  YYSYMBOL_equality_expression = 188,      /* equality_expression  */
  YYSYMBOL_and_expression = 189,           /* and_expression  */
  YYSYMBOL_exclusive_or_expression = 190,  /* exclusive_or_expression  */
  YYSYMBOL_inclusive_or_expression = 191,  /* inclusive_or_expression  */
  YYSYMBOL_logical_and_expression = 192,   /* logical_and_expression  */
  YYSYMBOL_logical_xor_expression = 193,   /* logical_xor_expression  */
  YYSYMBOL_logical_or_expression = 194,    /* logical_or_expression  */
  YYSYMBOL_conditional_expression = 195,   /* conditional_expression  */
  YYSYMBOL_assignment_expression = 196,    /* assignment_expression  */
  YYSYMBOL_assignment_operator = 197,      /* assignment_operator  */
  YYSYMBOL_expression = 198,               /* expression  */
  YYSYMBOL_constant_expression = 199,      /* constant_expression  */
  YYSYMBOL_declaration = 200,              /* declaration  */
  YYSYMBOL_function_prototype = 201,       /* function_prototype  */
  YYSYMBOL_function_declarator = 202,      /* function_declarator  */
  YYSYMBOL_function_header_with_parameters = 203, /* function_header_with_parameters  */
  YYSYMBOL_function_header = 204,          /* function_header  */
  YYSYMBOL_parameter_declarator = 205,     /* parameter_declarator  */
  YYSYMBOL_parameter_declaration = 206,    /* parameter_declaration  */
  YYSYMBOL_parameter_qualifier = 207,      /* parameter_qualifier  */
  YYSYMBOL_parameter_direction_qualifier = 208, /* parameter_direction_qualifier  */
  YYSYMBOL_parameter_type_specifier = 209, /* parameter_type_specifier  */
  YYSYMBOL_init_declarator_list = 210,     /* init_declarator_list  */
  YYSYMBOL_single_declaration = 211,       /* single_declaration  */
  YYSYMBOL_fully_specified_type = 212,     /* fully_specified_type  */
  YYSYMBOL_layout_qualifier = 213,         /* layout_qualifier  */
  YYSYMBOL_layout_qualifier_id_list = 214, /* layout_qualifier_id_list  */
  YYSYMBOL_layout_qualifier_id = 215,      /* layout_qualifier_id  */
  YYSYMBOL_interface_block_layout_qualifier = 216, /* interface_block_layout_qualifier  */
  YYSYMBOL_subroutine_qualifier = 217,     /* subroutine_qualifier  */
  YYSYMBOL_subroutine_type_list = 218,     /* subroutine_type_list  */
  YYSYMBOL_interpolation_qualifier = 219,  /* interpolation_qualifier  */
  YYSYMBOL_type_qualifier = 220,           /* type_qualifier  */
  YYSYMBOL_auxiliary_storage_qualifier = 221, /* auxiliary_storage_qualifier  */
  YYSYMBOL_storage_qualifier = 222,        /* storage_qualifier  */
  YYSYMBOL_memory_qualifier = 223,         /* memory_qualifier  */
  YYSYMBOL_array_specifier = 224,          /* array_specifier  */
  YYSYMBOL_type_specifier = 225,           /* type_specifier  */
  YYSYMBOL_type_specifier_nonarray = 226,  /* type_specifier_nonarray  */
  YYSYMBOL_basic_type_specifier_nonarray = 227, /* basic_type_specifier_nonarray  */
  YYSYMBOL_precision_qualifier = 228,      /* precision_qualifier  */
  YYSYMBOL_struct_specifier = 229,         /* struct_specifier  */
  YYSYMBOL_struct_declaration_list = 230,  /* struct_declaration_list  */
  YYSYMBOL_struct_declaration = 231,       /* struct_declaration  */
  YYSYMBOL_struct_declarator_list = 232,   /* struct_declarator_list  */
  YYSYMBOL_struct_declarator = 233,        /* struct_declarator  */
  YYSYMBOL_initializer = 234,              /* initializer  */
  YYSYMBOL_initializer_list = 235,         /* initializer_list  */
  YYSYMBOL_declaration_statement = 236,    /* declaration_statement  */
  YYSYMBOL_statement = 237,                /* statement  */
  YYSYMBOL_simple_statement = 238,         /* simple_statement  */
  YYSYMBOL_compound_statement = 239,       /* compound_statement  */
  YYSYMBOL_240_2 = 240,                    /* $@2  */
  YYSYMBOL_statement_no_new_scope = 241,   /* statement_no_new_scope  */
  YYSYMBOL_compound_statement_no_new_scope = 242, /* compound_statement_no_new_scope  */
  YYSYMBOL_statement_list = 243,           /* statement_list  */
  YYSYMBOL_expression_statement = 244,     /* expression_statement  */
  YYSYMBOL_selection_statement = 245,      /* selection_statement  */
  YYSYMBOL_selection_rest_statement = 246, /* selection_rest_statement  */
  YYSYMBOL_condition = 247,                /* condition  */
  YYSYMBOL_switch_statement = 248,         /* switch_statement  */
  YYSYMBOL_switch_body = 249,              /* switch_body  */
  YYSYMBOL_case_label = 250,               /* case_label  */
  YYSYMBOL_case_label_list = 251,          /* case_label_list  */
  YYSYMBOL_case_statement = 252,           /* case_statement  */
  YYSYMBOL_case_statement_list = 253,      /* case_statement_list  */
  YYSYMBOL_iteration_statement = 254,      /* iteration_statement  */
  YYSYMBOL_for_init_statement = 255,       /* for_init_statement  */
  YYSYMBOL_conditionopt = 256,             /* conditionopt  */
  YYSYMBOL_for_rest_statement = 257,       /* for_rest_statement  */
  YYSYMBOL_jump_statement = 258,           /* jump_statement  */
  YYSYMBOL_demote_statement = 259,         /* demote_statement  */
  YYSYMBOL_external_declaration = 260,     /* external_declaration  */
  YYSYMBOL_function_definition = 261,      /* function_definition  */
  YYSYMBOL_interface_block = 262,          /* interface_block  */
  YYSYMBOL_basic_interface_block = 263,    /* basic_interface_block  */
  YYSYMBOL_interface_qualifier = 264,      /* interface_qualifier  */
  YYSYMBOL_instance_name_opt = 265,        /* instance_name_opt  */
  YYSYMBOL_member_list = 266,              /* member_list  */
  YYSYMBOL_member_declaration = 267,       /* member_declaration  */
  YYSYMBOL_layout_uniform_defaults = 268,  /* layout_uniform_defaults  */
  YYSYMBOL_layout_buffer_defaults = 269,   /* layout_buffer_defaults  */
  YYSYMBOL_layout_in_defaults = 270,       /* layout_in_defaults  */
  YYSYMBOL_layout_out_defaults = 271,      /* layout_out_defaults  */
  YYSYMBOL_layout_defaults = 272           /* layout_defaults  */
};
typedef enum yysymbol_kind_t yysymbol_kind_t;




#ifdef short
# undef short
#endif

/* On compilers that do not define __PTRDIFF_MAX__ etc., make sure
   <limits.h> and (if available) <stdint.h> are included
   so that the code can choose integer types of a good width.  */

#ifndef __PTRDIFF_MAX__
# include <limits.h> /* INFRINGES ON USER NAME SPACE */
# if defined __STDC_VERSION__ && 199901 <= __STDC_VERSION__
#  include <stdint.h> /* INFRINGES ON USER NAME SPACE */
#  define YY_STDINT_H
# endif
#endif

/* Narrow types that promote to a signed type and that can represent a
   signed or unsigned integer of at least N bits.  In tables they can
   save space and decrease cache pressure.  Promoting to a signed type
   helps avoid bugs in integer arithmetic.  */

#ifdef __INT_LEAST8_MAX__
typedef __INT_LEAST8_TYPE__ yytype_int8;
#elif defined YY_STDINT_H
typedef int_least8_t yytype_int8;
#else
typedef signed char yytype_int8;
#endif

#ifdef __INT_LEAST16_MAX__
typedef __INT_LEAST16_TYPE__ yytype_int16;
#elif defined YY_STDINT_H
typedef int_least16_t yytype_int16;
#else
typedef short yytype_int16;
#endif

/* Work around bug in HP-UX 11.23, which defines these macros
   incorrectly for preprocessor constants.  This workaround can likely
   be removed in 2023, as HPE has promised support for HP-UX 11.23
   (aka HP-UX 11i v2) only through the end of 2022; see Table 2 of
   <https://h20195.www2.hpe.com/V2/getpdf.aspx/4AA4-7673ENW.pdf>.  */
#ifdef __hpux
# undef UINT_LEAST8_MAX
# undef UINT_LEAST16_MAX
# define UINT_LEAST8_MAX 255
# define UINT_LEAST16_MAX 65535
#endif

#if defined __UINT_LEAST8_MAX__ && __UINT_LEAST8_MAX__ <= __INT_MAX__
typedef __UINT_LEAST8_TYPE__ yytype_uint8;
#elif (!defined __UINT_LEAST8_MAX__ && defined YY_STDINT_H \
       && UINT_LEAST8_MAX <= INT_MAX)
typedef uint_least8_t yytype_uint8;
#elif !defined __UINT_LEAST8_MAX__ && UCHAR_MAX <= INT_MAX
typedef unsigned char yytype_uint8;
#else
typedef short yytype_uint8;
#endif

#if defined __UINT_LEAST16_MAX__ && __UINT_LEAST16_MAX__ <= __INT_MAX__
typedef __UINT_LEAST16_TYPE__ yytype_uint16;
#elif (!defined __UINT_LEAST16_MAX__ && defined YY_STDINT_H \
       && UINT_LEAST16_MAX <= INT_MAX)
typedef uint_least16_t yytype_uint16;
#elif !defined __UINT_LEAST16_MAX__ && USHRT_MAX <= INT_MAX
typedef unsigned short yytype_uint16;
#else
typedef int yytype_uint16;
#endif

#ifndef YYPTRDIFF_T
# if defined __PTRDIFF_TYPE__ && defined __PTRDIFF_MAX__
#  define YYPTRDIFF_T __PTRDIFF_TYPE__
#  define YYPTRDIFF_MAXIMUM __PTRDIFF_MAX__
# elif defined PTRDIFF_MAX
#  ifndef ptrdiff_t
#   include <stddef.h> /* INFRINGES ON USER NAME SPACE */
#  endif
#  define YYPTRDIFF_T ptrdiff_t
#  define YYPTRDIFF_MAXIMUM PTRDIFF_MAX
# else
#  define YYPTRDIFF_T long
#  define YYPTRDIFF_MAXIMUM LONG_MAX
# endif
#endif

#ifndef YYSIZE_T
# ifdef __SIZE_TYPE__
#  define YYSIZE_T __SIZE_TYPE__
# elif defined size_t
#  define YYSIZE_T size_t
# elif defined __STDC_VERSION__ && 199901 <= __STDC_VERSION__
#  include <stddef.h> /* INFRINGES ON USER NAME SPACE */
#  define YYSIZE_T size_t
# else
#  define YYSIZE_T unsigned
# endif
#endif

#define YYSIZE_MAXIMUM                                  \
  YY_CAST (YYPTRDIFF_T,                                 \
           (YYPTRDIFF_MAXIMUM < YY_CAST (YYSIZE_T, -1)  \
            ? YYPTRDIFF_MAXIMUM                         \
            : YY_CAST (YYSIZE_T, -1)))

#define YYSIZEOF(X) YY_CAST (YYPTRDIFF_T, sizeof (X))


/* Stored state numbers (used for stacks). */
typedef yytype_int16 yy_state_t;

/* State numbers in computations.  */
typedef int yy_state_fast_t;

#ifndef YY_
# if defined YYENABLE_NLS && YYENABLE_NLS
#  if ENABLE_NLS
#   include <libintl.h> /* INFRINGES ON USER NAME SPACE */
#   define YY_(Msgid) dgettext ("bison-runtime", Msgid)
#  endif
# endif
# ifndef YY_
#  define YY_(Msgid) Msgid
# endif
#endif


#ifndef YY_ATTRIBUTE_PURE
# if defined __GNUC__ && 2 < __GNUC__ + (96 <= __GNUC_MINOR__)
#  define YY_ATTRIBUTE_PURE __attribute__ ((__pure__))
# else
#  define YY_ATTRIBUTE_PURE
# endif
#endif

#ifndef YY_ATTRIBUTE_UNUSED
# if defined __GNUC__ && 2 < __GNUC__ + (7 <= __GNUC_MINOR__)
#  define YY_ATTRIBUTE_UNUSED __attribute__ ((__unused__))
# else
#  define YY_ATTRIBUTE_UNUSED
# endif
#endif

/* Suppress unused-variable warnings by "using" E.  */
#if ! defined lint || defined __GNUC__
# define YY_USE(E) ((void) (E))
#else
# define YY_USE(E) /* empty */
#endif

/* Suppress an incorrect diagnostic about yylval being uninitialized.  */
#if defined __GNUC__ && ! defined __ICC && 406 <= __GNUC__ * 100 + __GNUC_MINOR__
# if __GNUC__ * 100 + __GNUC_MINOR__ < 407
#  define YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN                           \
    _Pragma ("GCC diagnostic push")                                     \
    _Pragma ("GCC diagnostic ignored \"-Wuninitialized\"")
# else
#  define YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN                           \
    _Pragma ("GCC diagnostic push")                                     \
    _Pragma ("GCC diagnostic ignored \"-Wuninitialized\"")              \
    _Pragma ("GCC diagnostic ignored \"-Wmaybe-uninitialized\"")
# endif
# define YY_IGNORE_MAYBE_UNINITIALIZED_END      \
    _Pragma ("GCC diagnostic pop")
#else
# define YY_INITIAL_VALUE(Value) Value
#endif
#ifndef YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
# define YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
# define YY_IGNORE_MAYBE_UNINITIALIZED_END
#endif
#ifndef YY_INITIAL_VALUE
# define YY_INITIAL_VALUE(Value) /* Nothing. */
#endif

#if defined __cplusplus && defined __GNUC__ && ! defined __ICC && 6 <= __GNUC__
# define YY_IGNORE_USELESS_CAST_BEGIN                          \
    _Pragma ("GCC diagnostic push")                            \
    _Pragma ("GCC diagnostic ignored \"-Wuseless-cast\"")
# define YY_IGNORE_USELESS_CAST_END            \
    _Pragma ("GCC diagnostic pop")
#endif
#ifndef YY_IGNORE_USELESS_CAST_BEGIN
# define YY_IGNORE_USELESS_CAST_BEGIN
# define YY_IGNORE_USELESS_CAST_END
#endif


#define YY_ASSERT(E) ((void) (0 && (E)))

#if 1

/* The parser invokes alloca or malloc; define the necessary symbols.  */

# ifdef YYSTACK_USE_ALLOCA
#  if YYSTACK_USE_ALLOCA
#   ifdef __GNUC__
#    define YYSTACK_ALLOC __builtin_alloca
#   elif defined __BUILTIN_VA_ARG_INCR
#    include <alloca.h> /* INFRINGES ON USER NAME SPACE */
#   elif defined _AIX
#    define YYSTACK_ALLOC __alloca
#   elif defined _MSC_VER
#    include <malloc.h> /* INFRINGES ON USER NAME SPACE */
#    define alloca _alloca
#   else
#    define YYSTACK_ALLOC alloca
#    if ! defined _ALLOCA_H && ! defined EXIT_SUCCESS
#     include <stdlib.h> /* INFRINGES ON USER NAME SPACE */
      /* Use EXIT_SUCCESS as a witness for stdlib.h.  */
#     ifndef EXIT_SUCCESS
#      define EXIT_SUCCESS 0
#     endif
#    endif
#   endif
#  endif
# endif

# ifdef YYSTACK_ALLOC
   /* Pacify GCC's 'empty if-body' warning.  */
#  define YYSTACK_FREE(Ptr) do { /* empty */; } while (0)
#  ifndef YYSTACK_ALLOC_MAXIMUM
    /* The OS might guarantee only one guard page at the bottom of the stack,
       and a page size can be as small as 4096 bytes.  So we cannot safely
       invoke alloca (N) if N exceeds 4096.  Use a slightly smaller number
       to allow for a few compiler-allocated temporary stack slots.  */
#   define YYSTACK_ALLOC_MAXIMUM 4032 /* reasonable circa 2006 */
#  endif
# else
#  define YYSTACK_ALLOC YYMALLOC
#  define YYSTACK_FREE YYFREE
#  ifndef YYSTACK_ALLOC_MAXIMUM
#   define YYSTACK_ALLOC_MAXIMUM YYSIZE_MAXIMUM
#  endif
#  if (defined __cplusplus && ! defined EXIT_SUCCESS \
       && ! ((defined YYMALLOC || defined malloc) \
             && (defined YYFREE || defined free)))
#   include <stdlib.h> /* INFRINGES ON USER NAME SPACE */
#   ifndef EXIT_SUCCESS
#    define EXIT_SUCCESS 0
#   endif
#  endif
#  ifndef YYMALLOC
#   define YYMALLOC malloc
#   if ! defined malloc && ! defined EXIT_SUCCESS
void *malloc (YYSIZE_T); /* INFRINGES ON USER NAME SPACE */
#   endif
#  endif
#  ifndef YYFREE
#   define YYFREE free
#   if ! defined free && ! defined EXIT_SUCCESS
void free (void *); /* INFRINGES ON USER NAME SPACE */
#   endif
#  endif
# endif
#endif /* 1 */

#if (! defined yyoverflow \
     && (! defined __cplusplus \
         || (defined YYLTYPE_IS_TRIVIAL && YYLTYPE_IS_TRIVIAL \
             && defined YYSTYPE_IS_TRIVIAL && YYSTYPE_IS_TRIVIAL)))

/* A type that is properly aligned for any stack member.  */
union yyalloc
{
  yy_state_t yyss_alloc;
  YYSTYPE yyvs_alloc;
  YYLTYPE yyls_alloc;
};

/* The size of the maximum gap between one aligned stack and the next.  */
# define YYSTACK_GAP_MAXIMUM (YYSIZEOF (union yyalloc) - 1)

/* The size of an array large to enough to hold all stacks, each with
   N elements.  */
# define YYSTACK_BYTES(N) \
     ((N) * (YYSIZEOF (yy_state_t) + YYSIZEOF (YYSTYPE) \
             + YYSIZEOF (YYLTYPE)) \
      + 2 * YYSTACK_GAP_MAXIMUM)

# define YYCOPY_NEEDED 1

/* Relocate STACK from its old location to the new one.  The
   local variables YYSIZE and YYSTACKSIZE give the old and new number of
   elements in the stack, and YYPTR gives the new location of the
   stack.  Advance YYPTR to a properly aligned location for the next
   stack.  */
# define YYSTACK_RELOCATE(Stack_alloc, Stack)                           \
    do                                                                  \
      {                                                                 \
        YYPTRDIFF_T yynewbytes;                                         \
        YYCOPY (&yyptr->Stack_alloc, Stack, yysize);                    \
        Stack = &yyptr->Stack_alloc;                                    \
        yynewbytes = yystacksize * YYSIZEOF (*Stack) + YYSTACK_GAP_MAXIMUM; \
        yyptr += yynewbytes / YYSIZEOF (*yyptr);                        \
      }                                                                 \
    while (0)

#endif

#if defined YYCOPY_NEEDED && YYCOPY_NEEDED
/* Copy COUNT objects from SRC to DST.  The source and destination do
   not overlap.  */
# ifndef YYCOPY
#  if defined __GNUC__ && 1 < __GNUC__
#   define YYCOPY(Dst, Src, Count) \
      __builtin_memcpy (Dst, Src, YY_CAST (YYSIZE_T, (Count)) * sizeof (*(Src)))
#  else
#   define YYCOPY(Dst, Src, Count)              \
      do                                        \
        {                                       \
          YYPTRDIFF_T yyi;                      \
          for (yyi = 0; yyi < (Count); yyi++)   \
            (Dst)[yyi] = (Src)[yyi];            \
        }                                       \
      while (0)
#  endif
# endif
#endif /* !YYCOPY_NEEDED */

/* YYFINAL -- State number of the termination state.  */
#define YYFINAL  5
/* YYLAST -- Last index in YYTABLE.  */
#define YYLAST   2510

/* YYNTOKENS -- Number of terminals.  */
#define YYNTOKENS  162
/* YYNNTS -- Number of nonterminals.  */
#define YYNNTS  111
/* YYNRULES -- Number of rules.  */
#define YYNRULES  312
/* YYNSTATES -- Number of states.  */
#define YYNSTATES  475

/* YYMAXUTOK -- Last valid token kind.  */
#define YYMAXUTOK   393


/* YYTRANSLATE(TOKEN-NUM) -- Symbol number corresponding to TOKEN-NUM
   as returned by yylex, with out-of-bounds checking.  */
#define YYTRANSLATE(YYX)                                \
  (0 <= (YYX) && (YYX) <= YYMAXUTOK                     \
   ? YY_CAST (yysymbol_kind_t, yytranslate[YYX])        \
   : YYSYMBOL_YYUNDEF)

/* YYTRANSLATE[TOKEN-NUM] -- Symbol number corresponding to TOKEN-NUM
   as returned by yylex.  */
static const yytype_uint8 yytranslate[] =
{
       0,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,   146,     2,     2,     2,   150,   153,     2,
     139,   140,   148,   144,   143,   145,     2,   149,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,   157,   159,
     151,   158,   152,   156,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,   141,     2,   142,   154,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,   160,   155,   161,   147,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     1,     2,     3,     4,
       5,     6,     7,     8,     9,    10,    11,    12,    13,    14,
      15,    16,    17,    18,    19,    20,    21,    22,    23,    24,
      25,    26,    27,    28,    29,    30,    31,    32,    33,    34,
      35,    36,    37,    38,    39,    40,    41,    42,    43,    44,
      45,    46,    47,    48,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    58,    59,    60,    61,    62,    63,    64,
      65,    66,    67,    68,    69,    70,    71,    72,    73,    74,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    84,
      85,    86,    87,    88,    89,    90,    91,    92,    93,    94,
      95,    96,    97,    98,    99,   100,   101,   102,   103,   104,
     105,   106,   107,   108,   109,   110,   111,   112,   113,   114,
     115,   116,   117,   118,   119,   120,   121,   122,   123,   124,
     125,   126,   127,   128,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138
};

#if YYDEBUG
/* YYRLINE[YYN] -- Source line where rule number YYN was defined.  */
static const yytype_int16 yyrline[] =
{
       0,   295,   295,   294,   318,   320,   327,   337,   338,   339,
     340,   341,   365,   370,   377,   379,   383,   384,   385,   389,
     398,   406,   414,   425,   426,   430,   437,   444,   451,   458,
     465,   472,   479,   486,   493,   494,   500,   504,   511,   517,
     526,   530,   534,   538,   539,   543,   544,   548,   554,   566,
     570,   576,   590,   591,   597,   603,   613,   614,   615,   616,
     620,   621,   627,   633,   642,   643,   649,   658,   659,   665,
     674,   675,   681,   687,   693,   702,   703,   709,   718,   719,
     728,   729,   738,   739,   748,   749,   758,   759,   768,   769,
     778,   779,   788,   789,   798,   799,   800,   801,   802,   803,
     804,   805,   806,   807,   808,   812,   816,   832,   836,   841,
     845,   850,   867,   871,   872,   876,   881,   889,   907,   918,
     923,   938,   946,   963,   966,   974,   982,   994,  1006,  1013,
    1018,  1023,  1032,  1036,  1037,  1047,  1057,  1067,  1081,  1088,
    1099,  1110,  1121,  1132,  1144,  1159,  1166,  1184,  1191,  1192,
    1202,  1725,  1890,  1916,  1921,  1926,  1934,  1939,  1948,  1957,
    1969,  1974,  1979,  1988,  1993,  1998,  1999,  2000,  2001,  2002,
    2003,  2004,  2022,  2030,  2055,  2079,  2093,  2098,  2114,  2139,
    2151,  2159,  2164,  2169,  2176,  2181,  2186,  2191,  2196,  2221,
    2233,  2238,  2243,  2251,  2256,  2261,  2267,  2272,  2280,  2288,
    2294,  2304,  2315,  2316,  2324,  2330,  2336,  2345,  2346,  2347,
    2359,  2364,  2369,  2377,  2384,  2401,  2406,  2414,  2452,  2457,
    2465,  2471,  2480,  2481,  2485,  2492,  2499,  2506,  2512,  2513,
    2517,  2518,  2519,  2520,  2521,  2522,  2523,  2527,  2534,  2533,
    2547,  2548,  2552,  2558,  2567,  2577,  2586,  2598,  2604,  2613,
    2622,  2627,  2635,  2639,  2657,  2665,  2670,  2678,  2683,  2691,
    2699,  2707,  2715,  2723,  2731,  2739,  2746,  2753,  2763,  2764,
    2768,  2770,  2776,  2781,  2790,  2796,  2802,  2808,  2814,  2823,
    2832,  2833,  2834,  2835,  2836,  2840,  2854,  2858,  2871,  2889,
    2908,  2913,  2918,  2923,  2928,  2943,  2946,  2951,  2959,  2964,
    2972,  2996,  3003,  3007,  3014,  3018,  3028,  3037,  3047,  3056,
    3068,  3090,  3100
};
#endif

/** Accessing symbol of state STATE.  */
#define YY_ACCESSING_SYMBOL(State) YY_CAST (yysymbol_kind_t, yystos[State])

#if 1
/* The user-facing name of the symbol whose (internal) number is
   YYSYMBOL.  No bounds checking.  */
static const char *yysymbol_name (yysymbol_kind_t yysymbol) YY_ATTRIBUTE_UNUSED;

/* YYTNAME[SYMBOL-NUM] -- String name of the symbol SYMBOL-NUM.
   First, the terminals, then, starting at YYNTOKENS, nonterminals.  */
static const char *const yytname[] =
{
  "\"end of file\"", "error", "\"invalid token\"", "ATTRIBUTE",
  "CONST_TOK", "BASIC_TYPE_TOK", "BREAK", "BUFFER", "CONTINUE", "DO",
  "ELSE", "FOR", "IF", "DEMOTE", "DISCARD", "RETURN", "SWITCH", "CASE",
  "DEFAULT", "CENTROID", "IN_TOK", "OUT_TOK", "INOUT_TOK", "UNIFORM",
  "VARYING", "SAMPLE", "NOPERSPECTIVE", "FLAT", "SMOOTH", "IMAGE1DSHADOW",
  "IMAGE2DSHADOW", "IMAGE1DARRAYSHADOW", "IMAGE2DARRAYSHADOW", "COHERENT",
  "VOLATILE", "RESTRICT", "READONLY", "WRITEONLY", "SHARED", "STRUCT",
  "VOID_TOK", "WHILE", "IDENTIFIER", "TYPE_IDENTIFIER", "NEW_IDENTIFIER",
  "FLOATCONSTANT", "DOUBLECONSTANT", "INTCONSTANT", "UINTCONSTANT",
  "BOOLCONSTANT", "INT64CONSTANT", "UINT64CONSTANT", "FIELD_SELECTION",
  "LEFT_OP", "RIGHT_OP", "INC_OP", "DEC_OP", "LE_OP", "GE_OP", "EQ_OP",
  "NE_OP", "AND_OP", "OR_OP", "XOR_OP", "MUL_ASSIGN", "DIV_ASSIGN",
  "ADD_ASSIGN", "MOD_ASSIGN", "LEFT_ASSIGN", "RIGHT_ASSIGN", "AND_ASSIGN",
  "XOR_ASSIGN", "OR_ASSIGN", "SUB_ASSIGN", "INVARIANT", "PRECISE", "LOWP",
  "MEDIUMP", "HIGHP", "SUPERP", "PRECISION", "VERSION_TOK", "EXTENSION",
  "LINE", "COLON", "EOL", "INTERFACE_TOK", "OUTPUT", "PRAGMA_DEBUG_ON",
  "PRAGMA_DEBUG_OFF", "PRAGMA_OPTIMIZE_ON", "PRAGMA_OPTIMIZE_OFF",
  "PRAGMA_WARNING_ON", "PRAGMA_WARNING_OFF", "PRAGMA_INVARIANT_ALL",
  "LAYOUT_TOK", "DOT_TOK", "ASM", "CLASS", "UNION", "ENUM", "TYPEDEF",
  "TEMPLATE", "THIS", "PACKED_TOK", "GOTO", "INLINE_TOK", "NOINLINE",
  "PUBLIC_TOK", "STATIC", "EXTERN", "EXTERNAL", "LONG_TOK", "SHORT_TOK",
  "HALF", "FIXED_TOK", "UNSIGNED", "INPUT_TOK", "HVEC2", "HVEC3", "HVEC4",
  "FVEC2", "FVEC3", "FVEC4", "SAMPLER3DRECT", "SIZEOF", "CAST",
  "NAMESPACE", "USING", "RESOURCE", "PATCH", "SUBROUTINE", "ERROR_TOK",
  "COMMON", "PARTITION", "ACTIVE", "FILTER", "ROW_MAJOR", "THEN", "'('",
  "')'", "'['", "']'", "','", "'+'", "'-'", "'!'", "'~'", "'*'", "'/'",
  "'%'", "'<'", "'>'", "'&'", "'^'", "'|'", "'?'", "':'", "'='", "';'",
  "'{'", "'}'", "$accept", "translation_unit", "$@1", "version_statement",
  "pragma_statement", "extension_statement_list", "any_identifier",
  "extension_statement", "external_declaration_list",
  "variable_identifier", "primary_expression", "postfix_expression",
  "integer_expression", "function_call", "function_call_or_method",
  "function_call_generic", "function_call_header_no_parameters",
  "function_call_header_with_parameters", "function_call_header",
  "function_identifier", "unary_expression", "unary_operator",
  "multiplicative_expression", "additive_expression", "shift_expression",
  "relational_expression", "equality_expression", "and_expression",
  "exclusive_or_expression", "inclusive_or_expression",
  "logical_and_expression", "logical_xor_expression",
  "logical_or_expression", "conditional_expression",
  "assignment_expression", "assignment_operator", "expression",
  "constant_expression", "declaration", "function_prototype",
  "function_declarator", "function_header_with_parameters",
  "function_header", "parameter_declarator", "parameter_declaration",
  "parameter_qualifier", "parameter_direction_qualifier",
  "parameter_type_specifier", "init_declarator_list", "single_declaration",
  "fully_specified_type", "layout_qualifier", "layout_qualifier_id_list",
  "layout_qualifier_id", "interface_block_layout_qualifier",
  "subroutine_qualifier", "subroutine_type_list",
  "interpolation_qualifier", "type_qualifier",
  "auxiliary_storage_qualifier", "storage_qualifier", "memory_qualifier",
  "array_specifier", "type_specifier", "type_specifier_nonarray",
  "basic_type_specifier_nonarray", "precision_qualifier",
  "struct_specifier", "struct_declaration_list", "struct_declaration",
  "struct_declarator_list", "struct_declarator", "initializer",
  "initializer_list", "declaration_statement", "statement",
  "simple_statement", "compound_statement", "$@2",
  "statement_no_new_scope", "compound_statement_no_new_scope",
  "statement_list", "expression_statement", "selection_statement",
  "selection_rest_statement", "condition", "switch_statement",
  "switch_body", "case_label", "case_label_list", "case_statement",
  "case_statement_list", "iteration_statement", "for_init_statement",
  "conditionopt", "for_rest_statement", "jump_statement",
  "demote_statement", "external_declaration", "function_definition",
  "interface_block", "basic_interface_block", "interface_qualifier",
  "instance_name_opt", "member_list", "member_declaration",
  "layout_uniform_defaults", "layout_buffer_defaults",
  "layout_in_defaults", "layout_out_defaults", "layout_defaults", YY_NULLPTR
};

static const char *
yysymbol_name (yysymbol_kind_t yysymbol)
{
  return yytname[yysymbol];
}
#endif

#define YYPACT_NINF (-322)

#define yypact_value_is_default(Yyn) \
  ((Yyn) == YYPACT_NINF)

#define YYTABLE_NINF (-294)

#define yytable_value_is_error(Yyn) \
  0

/* YYPACT[STATE-NUM] -- Index in YYTABLE of the portion describing
   STATE-NUM.  */
static const yytype_int16 yypact[] =
{
     -66,    -3,    24,  -322,   120,  -322,   -33,  -322,  -322,  -322,
    -322,     7,   228,  1762,  -322,  -322,    26,  -322,  -322,  -322,
      74,  -322,    91,   116,  -322,   122,  -322,  -322,  -322,  -322,
    -322,  -322,  -322,  -322,  -322,  -322,  -322,   -23,  -322,  -322,
    2184,  2184,  -322,  -322,  -322,   210,    83,    94,   115,   129,
     131,   140,   143,    93,   193,  -322,    97,  -322,  -322,  1663,
    -322,    30,    90,   104,    64,  -114,  -322,   257,  2250,  2313,
    2313,    31,  2379,  2313,  2379,  -322,   119,  -322,  2313,  -322,
    -322,  -322,  -322,  -322,   213,  -322,  -322,  -322,  -322,  -322,
     228,  2121,   102,  -322,  -322,  -322,  -322,  -322,  -322,  2313,
    2313,  -322,  2313,  -322,  2313,  2313,  -322,  -322,    31,  -322,
    -322,  -322,  -322,  -322,  -322,  -322,    -1,  -322,   228,  -322,
    -322,  -322,   811,  -322,  -322,   543,   543,  -322,  -322,  -322,
     543,  -322,    51,   543,   543,   543,   228,  -322,   134,   137,
     -69,   144,   -31,   -16,   -14,   -11,  -322,  -322,  -322,  -322,
    -322,  -322,  -322,  -322,  -322,  -322,  -322,  -322,  2379,  -322,
    -322,  1855,   127,  -322,   132,   212,   228,   940,  -322,  2121,
     136,  -322,  -322,  -322,   167,   -29,  -322,  -322,  -322,    59,
     173,   174,  1290,   195,   197,   182,   183,  1768,   206,   208,
    -322,  -322,  -322,  -322,  -322,  -322,  -322,  1991,  1991,  1991,
    -322,  -322,  -322,  -322,  -322,   189,  -322,  -322,  -322,    96,
    -322,  -322,  -322,   150,    70,  2023,   214,   251,  1991,   156,
     107,   225,    25,   248,   199,   200,   201,   294,   295,   -54,
    -322,  -322,  -105,  -322,   198,   220,  -322,  -322,  -322,  -322,
     493,  -322,  -322,  -322,  -322,  -322,  -322,  -322,  -322,  -322,
    -322,  -322,    31,   228,  -322,  -322,  -322,    17,  1502,    20,
    -322,  -322,  -322,  -322,  -322,  -322,  -322,  -322,   218,  -322,
    1967,  2121,  -322,   119,   -47,  -322,  -322,  -322,  1003,  -322,
    1991,  -322,    -1,  -322,   228,  -322,  -322,  -322,   320,  -322,
    1577,  1991,  -322,  -322,  -322,    50,  1991,  1913,  -322,  -322,
      86,  -322,  1448,  -322,  -322,   311,  1991,  -322,  -322,  1991,
     224,  -322,  -322,  -322,  -322,  -322,  -322,  -322,  -322,  -322,
    -322,  -322,  -322,  -322,  1991,  -322,  1991,  1991,  1991,  1991,
    1991,  1991,  1991,  1991,  1991,  1991,  1991,  1991,  1991,  1991,
    1991,  1991,  1991,  1991,  1991,  1991,  1991,  -322,  -322,  -322,
    -322,   228,   119,  1502,    33,  1502,  -322,  -322,  1502,  -322,
    -322,   226,   228,   217,  2121,   127,   228,  -322,  -322,  -322,
    -322,  -322,   227,  -322,  -322,  1913,   105,  -322,   106,   236,
     228,   240,  -322,   652,  -322,   241,   236,  -322,  -322,  -322,
    -322,  -322,   156,   156,   107,   107,   225,   225,   225,   225,
      25,    25,   248,   199,   200,   201,   294,   295,  -109,  -322,
    -322,   127,  -322,  1502,  -322,  -104,  -322,  -322,    53,   323,
    -322,  -322,  1991,  -322,   223,   245,  1448,   229,   230,  1290,
    -322,  -322,  1991,  -322,   946,  -322,  -322,   119,   231,   113,
    1991,  1290,   376,  -322,    -8,  -322,  1502,  -322,  -322,  -322,
    -322,   127,  -322,   232,   236,  -322,  1448,  1991,   235,  -322,
    -322,  1132,  1448,    -6,  -322,  -322,  -322,   -70,  -322,  -322,
    -322,  -322,  -322,  1448,  -322
};

/* YYDEFACT[STATE-NUM] -- Default reduction number in state STATE-NUM.
   Performed when YYTABLE does not specify something else to do.  Zero
   means the default is an error.  */
static const yytype_int16 yydefact[] =
{
       4,     0,     0,    14,     0,     1,     2,    16,    17,    18,
       5,     0,     0,     0,    15,     6,     0,   185,   184,   208,
     191,   181,   187,   188,   189,   190,   186,   182,   162,   161,
     160,   193,   194,   195,   196,   197,   192,     0,   207,   206,
     163,   164,   212,   211,   210,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   183,   156,   284,   282,     3,
     281,     0,     0,   114,   123,     0,   133,   138,   168,   170,
     167,     0,   165,   166,   169,   145,   202,   204,   171,   205,
      20,   280,   111,   286,     0,   309,   310,   311,   312,   283,
       0,     0,     0,   191,   187,   188,   190,    23,    24,   163,
     164,   143,   168,   173,   165,   169,   144,   172,     0,     7,
       8,     9,    10,    12,    13,    11,     0,   209,     0,    22,
      21,   108,     0,   285,   112,   123,   123,   129,   130,   131,
     123,   115,     0,   123,   123,   123,     0,   109,    16,    18,
     139,     0,   191,   187,   188,   190,   175,   287,   301,   303,
     305,   307,   176,   174,   146,   177,   294,   178,   168,   180,
     288,     0,   203,   179,     0,     0,     0,     0,   215,     0,
       0,   155,   154,   153,   150,     0,   148,   152,   158,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      30,    31,    26,    27,    32,    28,    29,     0,     0,     0,
      56,    57,    58,    59,   247,   238,   242,    25,    34,    52,
      36,    41,    42,     0,     0,    46,     0,    60,     0,    64,
      67,    70,    75,    78,    80,    82,    84,    86,    88,    90,
      92,   105,     0,   227,     0,   145,   230,   244,   229,   228,
       0,   231,   232,   233,   234,   235,   236,   116,   124,   125,
     121,   122,     0,   132,   126,   128,   127,   134,     0,   140,
     117,   304,   306,   308,   302,   198,    60,   107,     0,    50,
       0,     0,    19,   220,     0,   218,   214,   216,     0,   110,
       0,   147,     0,   157,     0,   275,   274,   241,     0,   240,
       0,     0,   279,   278,   276,     0,     0,     0,    53,    54,
       0,   237,     0,    38,    39,     0,     0,    44,    43,     0,
     207,    47,    49,    95,    96,    98,    97,   100,   101,   102,
     103,   104,    99,    94,     0,    55,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   248,   243,   246,
     245,     0,   118,     0,   135,     0,   222,   142,     0,   199,
     200,     0,     0,     0,   298,   221,     0,   217,   213,   151,
     149,   159,     0,   269,   268,   271,     0,   277,     0,   252,
       0,     0,    33,     0,    37,     0,    40,    48,    93,    61,
      62,    63,    65,    66,    68,    69,    73,    74,    71,    72,
      76,    77,    79,    81,    83,    85,    87,    89,     0,   106,
     119,   120,   137,     0,   225,     0,   141,   201,     0,   295,
     299,   219,     0,   270,     0,     0,     0,     0,     0,     0,
     239,    35,     0,   136,     0,   223,   300,   296,     0,     0,
     272,     0,   251,   249,     0,   254,     0,   265,    91,   224,
     226,   297,   289,     0,   273,   267,     0,     0,     0,   255,
     259,     0,   263,     0,   253,   266,   250,     0,   258,   261,
     260,   262,   256,   264,   257
};

/* YYPGOTO[NTERM-NUM].  */
static const yytype_int16 yypgoto[] =
{
    -322,  -322,  -322,  -322,  -322,  -322,    14,    21,  -322,   130,
    -322,  -322,  -322,  -322,  -322,  -322,  -322,  -322,  -322,  -322,
     151,  -322,   -20,   -18,  -112,   -10,    48,    55,    52,    54,
      56,    57,  -322,  -136,  -152,  -322,  -111,  -165,    -7,    34,
    -322,  -322,  -322,  -322,   272,    85,  -322,  -322,  -322,  -322,
     -90,     1,  -322,   118,  -322,  -322,  -322,  -322,   303,   -38,
    -322,    -9,  -135,   -13,  -322,  -322,   205,  -322,   237,  -151,
      36,    38,  -171,  -322,   121,  -218,  -175,  -322,  -322,  -321,
     349,   110,   124,  -322,  -322,    40,  -322,  -322,   -43,  -322,
     -44,  -322,  -322,  -322,  -322,  -322,  -322,  -322,   363,  -322,
     -51,  -322,   351,  -322,    60,  -322,   357,   358,   361,   365,
    -322
};

/* YYDEFGOTO[NTERM-NUM].  */
static const yytype_int16 yydefgoto[] =
{
       0,     2,    13,     3,    58,     6,   273,   349,    59,   207,
     208,   209,   385,   210,   211,   212,   213,   214,   215,   216,
     217,   218,   219,   220,   221,   222,   223,   224,   225,   226,
     227,   228,   229,   230,   231,   324,   232,   268,   233,   234,
      62,    63,    64,   250,   131,   132,   133,   251,    65,    66,
      67,   102,   175,   176,   177,    69,   179,    70,    71,    72,
      73,   105,   162,   269,    76,    77,    78,    79,   167,   168,
     274,   275,   357,   415,   236,   237,   238,   239,   302,   288,
     289,   240,   241,   242,   443,   381,   243,   445,   460,   461,
     462,   463,   244,   375,   424,   425,   245,   246,    80,    81,
      82,    83,    84,   438,   363,   364,    85,    86,    87,    88,
      89
};

/* YYTABLE[YYPACT[STATE-NUM]] -- What to do in state STATE-NUM.  If
   positive, shift that token.  If negative, reduce the rule whose
   number is the opposite.  If YYTABLE_NINF, syntax error.  */
static const yytype_int16 yytable[] =
{
      75,   166,   104,   104,    74,   259,    60,   287,   344,   457,
     458,   457,   458,  -293,    68,     1,   277,   147,    11,     7,
       8,     9,   350,   160,     5,   267,    16,    14,  -290,   136,
    -291,   104,   104,  -292,   346,   104,    19,   171,   346,   434,
     104,     7,     8,     9,     4,   137,    75,    61,   432,    12,
      74,    92,    60,   104,   347,   134,    19,   435,   154,    74,
      68,   104,   104,   311,   104,    74,   104,   104,   126,    68,
      37,    38,   161,   346,    39,   158,   295,   166,    75,   166,
     119,   140,   333,   334,   127,   128,   129,   474,   300,   258,
      37,    38,    15,    61,    39,   170,   366,    31,    32,    33,
      34,    35,   345,   172,   165,   361,   356,   147,   447,   235,
      90,   281,   367,    74,   282,   369,   134,   134,  -293,   253,
     455,   134,   354,   158,   134,   134,   134,   277,   261,   104,
     174,   104,   178,   252,   267,  -290,   173,    91,   365,   130,
      42,    43,    44,   262,   267,   263,    53,    54,   264,    74,
     257,   303,   304,   459,    75,   472,    75,   387,   161,   158,
    -291,   270,     7,     8,     9,   350,  -292,    54,   109,   235,
     101,   106,   388,    74,   270,   353,   335,   336,   358,   110,
     376,   362,   412,   158,   414,   378,   379,   416,   166,   121,
     122,   413,   305,   346,   409,   386,   366,   141,   117,   283,
     111,   356,   284,   356,  -113,    10,   356,   380,   442,   377,
     308,   248,   436,   309,   112,   249,   113,   411,   254,   255,
     256,   396,   397,   398,   399,   114,   382,   235,   115,   346,
     124,    74,   116,   104,   408,   -51,   118,   306,   466,   351,
     104,   158,   433,   469,   471,   426,   427,   125,   346,   346,
     108,   329,   330,   453,   287,   471,   346,   164,    75,   104,
     161,   356,   169,   450,   379,    75,   287,   352,   270,   135,
       7,     8,     9,   -23,   362,   464,   -24,   235,   331,   332,
     448,    74,   356,   260,   235,   380,    42,    43,    44,   235,
     307,   158,   271,    74,   356,   279,   174,   272,   371,   138,
       8,   139,   451,   158,   326,   327,   328,   337,   338,   392,
     393,   439,   266,   394,   395,   313,   314,   315,   316,   317,
     318,   319,   320,   321,   322,   280,   104,   400,   401,   454,
     135,   135,   285,   286,   290,   135,   291,   104,   135,   135,
     135,   292,   293,   103,   107,   296,   467,   297,   298,   299,
     301,    75,   339,   312,   340,   342,   341,   121,   343,   -50,
     359,   372,   235,   384,   -45,   410,   422,   437,   417,   325,
     235,   146,   152,   153,    74,   155,   157,   159,   419,   346,
     429,   163,   440,   431,   158,   441,   456,   402,   446,   444,
     452,   465,   468,   404,   428,   403,   405,   247,   418,   406,
     370,   407,   103,   107,   421,   146,   278,   155,   159,   323,
     123,   373,   383,   235,   374,   423,   235,    74,   470,   473,
      74,   266,   120,   156,   420,   148,   149,   158,   235,   150,
     158,   266,    74,   151,     0,     0,     0,     0,     0,     0,
       0,     0,   158,   235,     0,     0,     0,    74,   235,   235,
       0,     0,    74,    74,     0,     0,     0,   158,     0,     0,
     235,   146,   158,   158,    74,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   158,     0,     0,   389,   390,   391,
     266,   266,   266,   266,   266,   266,   266,   266,   266,   266,
     266,   266,   266,   266,   266,   266,    17,    18,    19,   180,
      20,   181,   182,     0,   183,   184,   185,   186,   187,   188,
       0,     0,    21,    22,    23,    24,    25,    26,    27,    28,
      29,    30,     0,     0,     0,     0,    31,    32,    33,    34,
      35,    36,    37,    38,   189,    97,    39,    98,   190,   191,
     192,   193,   194,   195,   196,     0,     0,   126,   197,   198,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   127,   128,   129,     0,    40,    41,    42,
      43,    44,     0,    45,     0,    12,    31,    32,    33,    34,
      35,     0,     0,     0,     0,     0,     0,     0,    53,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    54,
       0,     0,     0,     0,     0,     0,     0,     0,   130,    42,
      43,    44,     0,    55,    56,     0,     0,     0,     0,     0,
       0,     0,   199,     0,     0,     0,     0,   200,   201,   202,
     203,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   204,   205,   348,    17,    18,    19,   180,    20,
     181,   182,     0,   183,   184,   185,   186,   187,   188,     0,
       0,    21,    22,    23,    24,    25,    26,    27,    28,    29,
      30,     0,     0,     0,     0,    31,    32,    33,    34,    35,
      36,    37,    38,   189,    97,    39,    98,   190,   191,   192,
     193,   194,   195,   196,     0,     0,     0,   197,   198,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    40,    41,    42,    43,
      44,     0,    45,     0,    12,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    53,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    54,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    55,    56,     0,     0,     0,     0,     0,     0,
       0,   199,     0,     0,     0,     0,   200,   201,   202,   203,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   204,   205,   430,    17,    18,    19,   180,    20,   181,
     182,     0,   183,   184,   185,   186,   187,   188,     0,     0,
      21,    22,    23,    24,    25,    26,    27,    28,    29,    30,
       0,     0,     0,     0,    31,    32,    33,    34,    35,    36,
      37,    38,   189,    97,    39,    98,   190,   191,   192,   193,
     194,   195,   196,     0,     0,     0,   197,   198,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    40,    41,    42,    43,    44,
       0,    45,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    53,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    54,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    55,    56,    17,    18,    19,     0,    93,     0,     0,
     199,    19,     0,     0,     0,   200,   201,   202,   203,    21,
      94,    95,    24,    96,    26,    27,    28,    29,    30,     0,
     204,   205,   206,    31,    32,    33,    34,    35,    36,    37,
      38,     0,     0,    39,     0,    37,    38,     0,    97,    39,
      98,   190,   191,   192,   193,   194,   195,   196,     0,     0,
       0,   197,   198,     0,     0,     0,    17,    18,    19,     0,
      93,     0,     0,     0,    99,   100,    42,    43,    44,     0,
       0,     0,    21,    94,    95,    24,    96,    26,    27,    28,
      29,    30,     0,     0,     0,    53,    31,    32,    33,    34,
      35,    36,    37,    38,     0,     0,    39,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    54,     0,     0,     0,
       0,     0,    54,     0,     0,     0,     0,     0,     0,     0,
      55,    56,     0,     0,     0,     0,     0,    99,   100,    42,
      43,    44,     0,     0,     0,   199,     0,     0,     0,     0,
     200,   201,   202,   203,     0,     0,     0,     0,    53,     0,
       0,   276,     0,     0,     0,     0,   355,   449,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    54,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    55,    56,    17,    18,    19,   180,    20,
     181,   182,     0,   183,   184,   185,   186,   187,   188,   457,
     458,    21,    22,    23,    24,    25,    26,    27,    28,    29,
      30,     0,     0,     0,   368,    31,    32,    33,    34,    35,
      36,    37,    38,   189,    97,    39,    98,   190,   191,   192,
     193,   194,   195,   196,     0,     0,     0,   197,   198,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    40,    41,    42,    43,
      44,     0,    45,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    53,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    54,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    55,    56,     0,     0,     0,     0,     0,     0,
       0,   199,     0,     0,     0,     0,   200,   201,   202,   203,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   204,   205,    17,    18,    19,   180,    20,   181,   182,
       0,   183,   184,   185,   186,   187,   188,     0,     0,    21,
      22,    23,    24,    25,    26,    27,    28,    29,    30,     0,
       0,     0,     0,    31,    32,    33,    34,    35,    36,    37,
      38,   189,    97,    39,    98,   190,   191,   192,   193,   194,
     195,   196,     0,     0,     0,   197,   198,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    40,    41,    42,    43,    44,     0,
      45,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,    53,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    54,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      55,    56,     0,     0,     0,     0,     0,     0,     0,   199,
       0,     0,     0,     0,   200,   201,   202,   203,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,   204,
     122,    17,    18,    19,   180,    20,   181,   182,     0,   183,
     184,   185,   186,   187,   188,     0,     0,    21,    22,    23,
      24,    25,    26,    27,    28,    29,    30,     0,     0,     0,
       0,    31,    32,    33,    34,    35,    36,    37,    38,   189,
      97,    39,    98,   190,   191,   192,   193,   194,   195,   196,
       0,     0,     0,   197,   198,     0,     0,    19,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    40,    41,    42,    43,    44,     0,    45,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    37,    38,    53,    97,    39,    98,   190,   191,   192,
     193,   194,   195,   196,     0,     0,     0,   197,   198,     0,
       0,     0,     0,     0,    54,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    55,    56,
      17,    18,    19,     0,    20,     0,     0,   199,     0,     0,
       0,     0,   200,   201,   202,   203,    21,    22,    23,    24,
      25,    26,    27,    28,    29,    30,     0,   204,   205,     0,
      31,    32,    33,    34,    35,    36,    37,    38,    54,    97,
      39,    98,   190,   191,   192,   193,   194,   195,   196,     0,
       0,     0,   197,   198,     0,     0,     0,     0,     0,     0,
       0,   199,     0,     0,     0,     0,   200,   201,   202,   203,
       0,    40,    41,    42,    43,    44,     0,    45,     0,     0,
       0,     0,   355,     0,     0,     0,    17,    18,    19,     0,
      20,     0,    53,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    21,    22,    23,    24,    25,    26,    27,    28,
      29,    30,     0,    54,     0,     0,    31,    32,    33,    34,
      35,    36,    37,    38,     0,     0,    39,    55,    56,     0,
       0,     0,     0,     0,     0,     0,   199,     0,     0,     0,
       0,   200,   201,   202,   203,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   204,    40,    41,    42,
      43,    44,     0,    45,     0,    12,     0,     0,     0,     0,
       0,    46,    47,    48,    49,    50,    51,    52,    53,     0,
       0,     0,     0,     0,     0,    17,    18,    19,     0,    20,
       0,     0,     0,    19,     0,     0,     0,     0,     0,    54,
       0,    21,    22,    23,    24,    25,    26,    27,    28,    29,
      30,     0,     0,    55,    56,    31,    32,    33,    34,    35,
      36,    37,    38,     0,     0,    39,     0,    37,    38,     0,
      97,    39,    98,   190,   191,   192,   193,   194,   195,   196,
       0,     0,    57,   197,   198,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    40,    41,    42,    43,
      44,     0,    45,     0,     0,     0,     0,     0,     0,     0,
      46,    47,    48,    49,    50,    51,    52,    53,     0,     0,
      19,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    54,     0,
       0,     0,     0,     0,    54,     0,     0,     0,     0,     0,
       0,     0,    55,    56,    37,    38,     0,    97,    39,    98,
     190,   191,   192,   193,   194,   195,   196,   199,     0,     0,
     197,   198,   200,   201,   202,   203,    17,    18,    19,     0,
      93,    57,     0,     0,     0,     0,     0,   294,     0,     0,
       0,     0,    21,    94,    95,    24,    96,    26,    27,    28,
      29,    30,     0,     0,     0,     0,    31,    32,    33,    34,
      35,    36,    37,    38,     0,    97,    39,    98,   190,   191,
     192,   193,   194,   195,   196,     0,     0,     0,   197,   198,
       0,    54,    19,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    99,   100,    42,
      43,    44,     0,     0,   199,     0,    19,   265,     0,   200,
     201,   202,   203,     0,     0,     0,    37,    38,    53,    97,
      39,    98,   190,   191,   192,   193,   194,   195,   196,     0,
       0,     0,   197,   198,     0,     0,     0,     0,    19,    54,
      37,    38,     0,    97,    39,    98,   190,   191,   192,   193,
     194,   195,   196,    55,    56,     0,   197,   198,     0,     0,
       0,     0,   199,     0,     0,     0,     0,   200,   201,   202,
     203,     0,    37,   310,     0,    97,    39,    98,   190,   191,
     192,   193,   194,   195,   196,     0,     0,     0,   197,   198,
       0,     0,     0,    54,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   199,    54,     0,   360,
       0,   200,   201,   202,   203,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    17,    18,    19,     0,    93,     0,
     199,     0,     0,     0,     0,   200,   201,   202,   203,    54,
      21,    94,    95,    24,    96,    26,    27,    28,    29,    30,
       0,     0,     0,     0,    31,    32,    33,    34,    35,    36,
      37,    38,   199,     0,    39,     0,     0,   200,   201,   202,
     203,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    17,    18,     0,
       0,    93,     0,     0,     0,    99,   100,    42,    43,    44,
       0,     0,     0,    21,    94,    95,    24,    96,    26,    27,
      28,    29,    30,     0,     0,     0,    53,    31,    32,    33,
      34,    35,    36,     0,     0,     0,    97,     0,    98,     0,
       0,     0,     0,     0,     0,     0,     0,    54,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    55,    56,    17,    18,     0,     0,   142,    99,   100,
      42,    43,    44,     0,     0,     0,     0,     0,     0,    21,
     143,   144,    24,   145,    26,    27,    28,    29,    30,    53,
       0,     0,     0,    31,    32,    33,    34,    35,    36,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    55,    56,    17,    18,     0,     0,
      93,     0,     0,     0,    99,   100,    42,    43,    44,     0,
       0,     0,    21,    94,    95,    24,    96,    26,    27,    28,
      29,    30,     0,     0,     0,    53,    31,    32,    33,    34,
      35,    36,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      55,    56,    17,    18,     0,     0,    20,    99,   100,    42,
      43,    44,     0,     0,     0,     0,     0,     0,    21,    22,
      23,    24,    25,    26,    27,    28,    29,    30,    53,     0,
       0,     0,    31,    32,    33,    34,    35,    36,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    55,    56,     0,     0,     0,     0,     0,
       0,     0,     0,    99,   100,    42,    43,    44,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    53,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    55,
      56
};

static const yytype_int16 yycheck[] =
{
      13,    91,    40,    41,    13,   140,    13,   182,    62,    17,
      18,    17,    18,    44,    13,    81,   167,    68,     4,    42,
      43,    44,   240,    74,     0,   161,    12,     6,    44,   143,
      44,    69,    70,    44,   143,    73,     5,    38,   143,   143,
      78,    42,    43,    44,    47,   159,    59,    13,   157,    82,
      59,    37,    59,    91,   159,    64,     5,   161,    71,    68,
      59,    99,   100,   215,   102,    74,   104,   105,     4,    68,
      39,    40,   141,   143,    43,    74,   187,   167,    91,   169,
      59,    67,    57,    58,    20,    21,    22,   157,   199,   158,
      39,    40,    85,    59,    43,   108,   143,    33,    34,    35,
      36,    37,   156,   104,    90,   270,   258,   158,   429,   122,
      84,   140,   159,   122,   143,   280,   125,   126,    44,   132,
     441,   130,   257,   122,   133,   134,   135,   278,   159,   167,
     116,   169,   118,   132,   270,    44,   137,   160,   273,    75,
      76,    77,    78,   159,   280,   159,    95,   116,   159,   158,
     136,    55,    56,   161,   167,   161,   169,   309,   141,   158,
      44,   141,    42,    43,    44,   383,    44,   116,    85,   182,
      40,    41,   324,   182,   141,   158,   151,   152,   158,    85,
     291,   271,   353,   182,   355,   296,   297,   358,   278,   159,
     160,   158,    96,   143,   346,   306,   143,    67,     5,   140,
      85,   353,   143,   355,   140,    85,   358,   297,   426,   159,
     140,   126,   159,   143,    85,   130,    85,   352,   133,   134,
     135,   333,   334,   335,   336,    85,   140,   240,    85,   143,
     140,   240,   139,   271,   345,   139,   139,   141,   456,   252,
     278,   240,   413,   461,   462,   140,   140,   143,   143,   143,
      45,   144,   145,   140,   429,   473,   143,    44,   271,   297,
     141,   413,   160,   434,   375,   278,   441,   253,   141,    64,
      42,    43,    44,   139,   364,   446,   139,   290,    53,    54,
     432,   290,   434,   139,   297,   375,    76,    77,    78,   302,
     140,   290,   160,   302,   446,   159,   282,    85,   284,    42,
      43,    44,   437,   302,   148,   149,   150,    59,    60,   329,
     330,   422,   161,   331,   332,    64,    65,    66,    67,    68,
      69,    70,    71,    72,    73,   158,   364,   337,   338,   440,
     125,   126,   159,   159,   139,   130,   139,   375,   133,   134,
     135,   159,   159,    40,    41,   139,   457,   139,   197,   198,
     161,   364,   153,   139,   154,    61,   155,   159,    63,   139,
     142,    41,   375,    52,   140,   351,   139,    44,   142,   218,
     383,    68,    69,    70,   383,    72,    73,    74,   161,   143,
     140,    78,   159,   142,   383,   140,    10,   339,   158,   160,
     159,   159,   157,   341,   380,   340,   342,   125,   362,   343,
     282,   344,    99,   100,   366,   102,   169,   104,   105,   158,
      61,   290,   302,   426,   290,   375,   429,   426,   461,   463,
     429,   270,    59,    72,   364,    68,    68,   426,   441,    68,
     429,   280,   441,    68,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   441,   456,    -1,    -1,    -1,   456,   461,   462,
      -1,    -1,   461,   462,    -1,    -1,    -1,   456,    -1,    -1,
     473,   158,   461,   462,   473,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   473,    -1,    -1,   326,   327,   328,
     329,   330,   331,   332,   333,   334,   335,   336,   337,   338,
     339,   340,   341,   342,   343,   344,     3,     4,     5,     6,
       7,     8,     9,    -1,    11,    12,    13,    14,    15,    16,
      -1,    -1,    19,    20,    21,    22,    23,    24,    25,    26,
      27,    28,    -1,    -1,    -1,    -1,    33,    34,    35,    36,
      37,    38,    39,    40,    41,    42,    43,    44,    45,    46,
      47,    48,    49,    50,    51,    -1,    -1,     4,    55,    56,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    20,    21,    22,    -1,    74,    75,    76,
      77,    78,    -1,    80,    -1,    82,    33,    34,    35,    36,
      37,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    95,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   116,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    75,    76,
      77,    78,    -1,   130,   131,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   139,    -1,    -1,    -1,    -1,   144,   145,   146,
     147,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   159,   160,   161,     3,     4,     5,     6,     7,
       8,     9,    -1,    11,    12,    13,    14,    15,    16,    -1,
      -1,    19,    20,    21,    22,    23,    24,    25,    26,    27,
      28,    -1,    -1,    -1,    -1,    33,    34,    35,    36,    37,
      38,    39,    40,    41,    42,    43,    44,    45,    46,    47,
      48,    49,    50,    51,    -1,    -1,    -1,    55,    56,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    74,    75,    76,    77,
      78,    -1,    80,    -1,    82,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    95,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   116,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   130,   131,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   139,    -1,    -1,    -1,    -1,   144,   145,   146,   147,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   159,   160,   161,     3,     4,     5,     6,     7,     8,
       9,    -1,    11,    12,    13,    14,    15,    16,    -1,    -1,
      19,    20,    21,    22,    23,    24,    25,    26,    27,    28,
      -1,    -1,    -1,    -1,    33,    34,    35,    36,    37,    38,
      39,    40,    41,    42,    43,    44,    45,    46,    47,    48,
      49,    50,    51,    -1,    -1,    -1,    55,    56,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    74,    75,    76,    77,    78,
      -1,    80,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    95,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   116,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   130,   131,     3,     4,     5,    -1,     7,    -1,    -1,
     139,     5,    -1,    -1,    -1,   144,   145,   146,   147,    19,
      20,    21,    22,    23,    24,    25,    26,    27,    28,    -1,
     159,   160,   161,    33,    34,    35,    36,    37,    38,    39,
      40,    -1,    -1,    43,    -1,    39,    40,    -1,    42,    43,
      44,    45,    46,    47,    48,    49,    50,    51,    -1,    -1,
      -1,    55,    56,    -1,    -1,    -1,     3,     4,     5,    -1,
       7,    -1,    -1,    -1,    74,    75,    76,    77,    78,    -1,
      -1,    -1,    19,    20,    21,    22,    23,    24,    25,    26,
      27,    28,    -1,    -1,    -1,    95,    33,    34,    35,    36,
      37,    38,    39,    40,    -1,    -1,    43,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   116,    -1,    -1,    -1,
      -1,    -1,   116,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     130,   131,    -1,    -1,    -1,    -1,    -1,    74,    75,    76,
      77,    78,    -1,    -1,    -1,   139,    -1,    -1,    -1,    -1,
     144,   145,   146,   147,    -1,    -1,    -1,    -1,    95,    -1,
      -1,   161,    -1,    -1,    -1,    -1,   160,   161,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   116,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   130,   131,     3,     4,     5,     6,     7,
       8,     9,    -1,    11,    12,    13,    14,    15,    16,    17,
      18,    19,    20,    21,    22,    23,    24,    25,    26,    27,
      28,    -1,    -1,    -1,   161,    33,    34,    35,    36,    37,
      38,    39,    40,    41,    42,    43,    44,    45,    46,    47,
      48,    49,    50,    51,    -1,    -1,    -1,    55,    56,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    74,    75,    76,    77,
      78,    -1,    80,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    95,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   116,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   130,   131,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   139,    -1,    -1,    -1,    -1,   144,   145,   146,   147,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   159,   160,     3,     4,     5,     6,     7,     8,     9,
      -1,    11,    12,    13,    14,    15,    16,    -1,    -1,    19,
      20,    21,    22,    23,    24,    25,    26,    27,    28,    -1,
      -1,    -1,    -1,    33,    34,    35,    36,    37,    38,    39,
      40,    41,    42,    43,    44,    45,    46,    47,    48,    49,
      50,    51,    -1,    -1,    -1,    55,    56,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    74,    75,    76,    77,    78,    -1,
      80,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    95,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   116,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     130,   131,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   139,
      -1,    -1,    -1,    -1,   144,   145,   146,   147,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   159,
     160,     3,     4,     5,     6,     7,     8,     9,    -1,    11,
      12,    13,    14,    15,    16,    -1,    -1,    19,    20,    21,
      22,    23,    24,    25,    26,    27,    28,    -1,    -1,    -1,
      -1,    33,    34,    35,    36,    37,    38,    39,    40,    41,
      42,    43,    44,    45,    46,    47,    48,    49,    50,    51,
      -1,    -1,    -1,    55,    56,    -1,    -1,     5,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    74,    75,    76,    77,    78,    -1,    80,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    39,    40,    95,    42,    43,    44,    45,    46,    47,
      48,    49,    50,    51,    -1,    -1,    -1,    55,    56,    -1,
      -1,    -1,    -1,    -1,   116,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   130,   131,
       3,     4,     5,    -1,     7,    -1,    -1,   139,    -1,    -1,
      -1,    -1,   144,   145,   146,   147,    19,    20,    21,    22,
      23,    24,    25,    26,    27,    28,    -1,   159,   160,    -1,
      33,    34,    35,    36,    37,    38,    39,    40,   116,    42,
      43,    44,    45,    46,    47,    48,    49,    50,    51,    -1,
      -1,    -1,    55,    56,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   139,    -1,    -1,    -1,    -1,   144,   145,   146,   147,
      -1,    74,    75,    76,    77,    78,    -1,    80,    -1,    -1,
      -1,    -1,   160,    -1,    -1,    -1,     3,     4,     5,    -1,
       7,    -1,    95,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    19,    20,    21,    22,    23,    24,    25,    26,
      27,    28,    -1,   116,    -1,    -1,    33,    34,    35,    36,
      37,    38,    39,    40,    -1,    -1,    43,   130,   131,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   139,    -1,    -1,    -1,
      -1,   144,   145,   146,   147,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   159,    74,    75,    76,
      77,    78,    -1,    80,    -1,    82,    -1,    -1,    -1,    -1,
      -1,    88,    89,    90,    91,    92,    93,    94,    95,    -1,
      -1,    -1,    -1,    -1,    -1,     3,     4,     5,    -1,     7,
      -1,    -1,    -1,     5,    -1,    -1,    -1,    -1,    -1,   116,
      -1,    19,    20,    21,    22,    23,    24,    25,    26,    27,
      28,    -1,    -1,   130,   131,    33,    34,    35,    36,    37,
      38,    39,    40,    -1,    -1,    43,    -1,    39,    40,    -1,
      42,    43,    44,    45,    46,    47,    48,    49,    50,    51,
      -1,    -1,   159,    55,    56,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    74,    75,    76,    77,
      78,    -1,    80,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      88,    89,    90,    91,    92,    93,    94,    95,    -1,    -1,
       5,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   116,    -1,
      -1,    -1,    -1,    -1,   116,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   130,   131,    39,    40,    -1,    42,    43,    44,
      45,    46,    47,    48,    49,    50,    51,   139,    -1,    -1,
      55,    56,   144,   145,   146,   147,     3,     4,     5,    -1,
       7,   159,    -1,    -1,    -1,    -1,    -1,   159,    -1,    -1,
      -1,    -1,    19,    20,    21,    22,    23,    24,    25,    26,
      27,    28,    -1,    -1,    -1,    -1,    33,    34,    35,    36,
      37,    38,    39,    40,    -1,    42,    43,    44,    45,    46,
      47,    48,    49,    50,    51,    -1,    -1,    -1,    55,    56,
      -1,   116,     5,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    74,    75,    76,
      77,    78,    -1,    -1,   139,    -1,     5,   142,    -1,   144,
     145,   146,   147,    -1,    -1,    -1,    39,    40,    95,    42,
      43,    44,    45,    46,    47,    48,    49,    50,    51,    -1,
      -1,    -1,    55,    56,    -1,    -1,    -1,    -1,     5,   116,
      39,    40,    -1,    42,    43,    44,    45,    46,    47,    48,
      49,    50,    51,   130,   131,    -1,    55,    56,    -1,    -1,
      -1,    -1,   139,    -1,    -1,    -1,    -1,   144,   145,   146,
     147,    -1,    39,    40,    -1,    42,    43,    44,    45,    46,
      47,    48,    49,    50,    51,    -1,    -1,    -1,    55,    56,
      -1,    -1,    -1,   116,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   139,   116,    -1,   142,
      -1,   144,   145,   146,   147,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,     3,     4,     5,    -1,     7,    -1,
     139,    -1,    -1,    -1,    -1,   144,   145,   146,   147,   116,
      19,    20,    21,    22,    23,    24,    25,    26,    27,    28,
      -1,    -1,    -1,    -1,    33,    34,    35,    36,    37,    38,
      39,    40,   139,    -1,    43,    -1,    -1,   144,   145,   146,
     147,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,     3,     4,    -1,
      -1,     7,    -1,    -1,    -1,    74,    75,    76,    77,    78,
      -1,    -1,    -1,    19,    20,    21,    22,    23,    24,    25,
      26,    27,    28,    -1,    -1,    -1,    95,    33,    34,    35,
      36,    37,    38,    -1,    -1,    -1,    42,    -1,    44,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   116,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   130,   131,     3,     4,    -1,    -1,     7,    74,    75,
      76,    77,    78,    -1,    -1,    -1,    -1,    -1,    -1,    19,
      20,    21,    22,    23,    24,    25,    26,    27,    28,    95,
      -1,    -1,    -1,    33,    34,    35,    36,    37,    38,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   130,   131,     3,     4,    -1,    -1,
       7,    -1,    -1,    -1,    74,    75,    76,    77,    78,    -1,
      -1,    -1,    19,    20,    21,    22,    23,    24,    25,    26,
      27,    28,    -1,    -1,    -1,    95,    33,    34,    35,    36,
      37,    38,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     130,   131,     3,     4,    -1,    -1,     7,    74,    75,    76,
      77,    78,    -1,    -1,    -1,    -1,    -1,    -1,    19,    20,
      21,    22,    23,    24,    25,    26,    27,    28,    95,    -1,
      -1,    -1,    33,    34,    35,    36,    37,    38,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   130,   131,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    74,    75,    76,    77,    78,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    95,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   130,
     131
};

/* YYSTOS[STATE-NUM] -- The symbol kind of the accessing symbol of
   state STATE-NUM.  */
static const yytype_int16 yystos[] =
{
       0,    81,   163,   165,    47,     0,   167,    42,    43,    44,
      85,   168,    82,   164,   169,    85,   168,     3,     4,     5,
       7,    19,    20,    21,    22,    23,    24,    25,    26,    27,
      28,    33,    34,    35,    36,    37,    38,    39,    40,    43,
      74,    75,    76,    77,    78,    80,    88,    89,    90,    91,
      92,    93,    94,    95,   116,   130,   131,   159,   166,   170,
     200,   201,   202,   203,   204,   210,   211,   212,   213,   217,
     219,   220,   221,   222,   223,   225,   226,   227,   228,   229,
     260,   261,   262,   263,   264,   268,   269,   270,   271,   272,
      84,   160,   168,     7,    20,    21,    23,    42,    44,    74,
      75,   171,   213,   220,   221,   223,   171,   220,   228,    85,
      85,    85,    85,    85,    85,    85,   139,     5,   139,   169,
     260,   159,   160,   242,   140,   143,     4,    20,    21,    22,
      75,   206,   207,   208,   223,   228,   143,   159,    42,    44,
     168,   171,     7,    20,    21,    23,   220,   262,   268,   269,
     270,   271,   220,   220,   225,   220,   264,   220,   213,   220,
     262,   141,   224,   220,    44,   168,   212,   230,   231,   160,
     225,    38,   104,   137,   168,   214,   215,   216,   168,   218,
       6,     8,     9,    11,    12,    13,    14,    15,    16,    41,
      45,    46,    47,    48,    49,    50,    51,    55,    56,   139,
     144,   145,   146,   147,   159,   160,   161,   171,   172,   173,
     175,   176,   177,   178,   179,   180,   181,   182,   183,   184,
     185,   186,   187,   188,   189,   190,   191,   192,   193,   194,
     195,   196,   198,   200,   201,   225,   236,   237,   238,   239,
     243,   244,   245,   248,   254,   258,   259,   206,   207,   207,
     205,   209,   213,   225,   207,   207,   207,   168,   158,   224,
     139,   159,   159,   159,   159,   142,   182,   195,   199,   225,
     141,   160,    85,   168,   232,   233,   161,   231,   230,   159,
     158,   140,   143,   140,   143,   159,   159,   238,   241,   242,
     139,   139,   159,   159,   159,   198,   139,   139,   182,   182,
     198,   161,   240,    55,    56,    96,   141,   140,   140,   143,
      40,   196,   139,    64,    65,    66,    67,    68,    69,    70,
      71,    72,    73,   158,   197,   182,   148,   149,   150,   144,
     145,    53,    54,    57,    58,   151,   152,    59,    60,   153,
     154,   155,    61,    63,    62,   156,   143,   159,   161,   169,
     237,   225,   168,   158,   224,   160,   196,   234,   158,   142,
     142,   199,   212,   266,   267,   224,   143,   159,   161,   199,
     215,   168,    41,   236,   244,   255,   198,   159,   198,   198,
     212,   247,   140,   243,    52,   174,   198,   196,   196,   182,
     182,   182,   184,   184,   185,   185,   186,   186,   186,   186,
     187,   187,   188,   189,   190,   191,   192,   193,   198,   196,
     168,   224,   234,   158,   234,   235,   234,   142,   232,   161,
     266,   233,   139,   247,   256,   257,   140,   140,   168,   140,
     161,   142,   157,   234,   143,   161,   159,    44,   265,   198,
     159,   140,   237,   246,   160,   249,   158,   241,   196,   161,
     234,   224,   159,   140,   198,   241,    10,    17,    18,   161,
     250,   251,   252,   253,   234,   159,   237,   198,   157,   237,
     250,   237,   161,   252,   157
};

/* YYR1[RULE-NUM] -- Symbol kind of the left-hand side of rule RULE-NUM.  */
static const yytype_int16 yyr1[] =
{
       0,   162,   164,   163,   165,   165,   165,   166,   166,   166,
     166,   166,   166,   166,   167,   167,   168,   168,   168,   169,
     170,   170,   170,   171,   171,   172,   172,   172,   172,   172,
     172,   172,   172,   172,   173,   173,   173,   173,   173,   173,
     174,   175,   176,   177,   177,   178,   178,   179,   179,   180,
     181,   181,   182,   182,   182,   182,   183,   183,   183,   183,
     184,   184,   184,   184,   185,   185,   185,   186,   186,   186,
     187,   187,   187,   187,   187,   188,   188,   188,   189,   189,
     190,   190,   191,   191,   192,   192,   193,   193,   194,   194,
     195,   195,   196,   196,   197,   197,   197,   197,   197,   197,
     197,   197,   197,   197,   197,   198,   198,   199,   200,   200,
     200,   200,   201,   202,   202,   203,   203,   204,   205,   205,
     205,   206,   206,   207,   207,   207,   207,   207,   207,   208,
     208,   208,   209,   210,   210,   210,   210,   210,   211,   211,
     211,   211,   211,   211,   211,   212,   212,   213,   214,   214,
     215,   215,   215,   216,   216,   216,   217,   217,   218,   218,
     219,   219,   219,   220,   220,   220,   220,   220,   220,   220,
     220,   220,   220,   220,   220,   220,   220,   220,   220,   220,
     220,   221,   221,   221,   222,   222,   222,   222,   222,   222,
     222,   222,   222,   223,   223,   223,   223,   223,   224,   224,
     224,   224,   225,   225,   226,   226,   226,   227,   227,   227,
     228,   228,   228,   229,   229,   230,   230,   231,   232,   232,
     233,   233,   234,   234,   234,   235,   235,   236,   237,   237,
     238,   238,   238,   238,   238,   238,   238,   239,   240,   239,
     241,   241,   242,   242,   243,   243,   243,   244,   244,   245,
     246,   246,   247,   247,   248,   249,   249,   250,   250,   251,
     251,   252,   252,   253,   253,   254,   254,   254,   255,   255,
     256,   256,   257,   257,   258,   258,   258,   258,   258,   259,
     260,   260,   260,   260,   260,   261,   262,   262,   262,   263,
     264,   264,   264,   264,   264,   265,   265,   265,   266,   266,
     267,   268,   268,   269,   269,   270,   270,   271,   271,   272,
     272,   272,   272
};

/* YYR2[RULE-NUM] -- Number of symbols on the right-hand side of rule RULE-NUM.  */
static const yytype_int8 yyr2[] =
{
       0,     2,     0,     4,     0,     3,     4,     2,     2,     2,
       2,     2,     2,     2,     0,     2,     1,     1,     1,     5,
       1,     2,     2,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     3,     1,     4,     1,     3,     2,     2,
       1,     1,     1,     2,     2,     2,     1,     2,     3,     2,
       1,     1,     1,     2,     2,     2,     1,     1,     1,     1,
       1,     3,     3,     3,     1,     3,     3,     1,     3,     3,
       1,     3,     3,     3,     3,     1,     3,     3,     1,     3,
       1,     3,     1,     3,     1,     3,     1,     3,     1,     3,
       1,     5,     1,     3,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     3,     1,     2,     2,
       4,     1,     2,     1,     1,     2,     3,     3,     2,     3,
       3,     2,     2,     0,     2,     2,     2,     2,     2,     1,
       1,     1,     1,     1,     3,     4,     6,     5,     1,     2,
       3,     5,     4,     2,     2,     1,     2,     4,     1,     3,
       1,     3,     1,     1,     1,     1,     1,     4,     1,     3,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     2,     3,
       3,     4,     1,     2,     1,     1,     1,     1,     1,     2,
       1,     1,     1,     5,     4,     1,     2,     3,     1,     3,
       1,     2,     1,     3,     4,     1,     3,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     2,     0,     4,
       1,     1,     2,     3,     1,     2,     2,     1,     2,     5,
       3,     1,     1,     4,     5,     2,     3,     3,     2,     1,
       2,     2,     2,     1,     2,     5,     7,     6,     1,     1,
       1,     0,     2,     3,     2,     2,     2,     3,     2,     2,
       1,     1,     1,     1,     1,     2,     1,     2,     2,     7,
       1,     1,     1,     1,     2,     0,     1,     2,     1,     2,
       3,     2,     3,     2,     3,     2,     3,     2,     3,     1,
       1,     1,     1
};


enum { YYENOMEM = -2 };

#define yyerrok         (yyerrstatus = 0)
#define yyclearin       (yychar = YYEMPTY)

#define YYACCEPT        goto yyacceptlab
#define YYABORT         goto yyabortlab
#define YYERROR         goto yyerrorlab
#define YYNOMEM         goto yyexhaustedlab


#define YYRECOVERING()  (!!yyerrstatus)

#define YYBACKUP(Token, Value)                                    \
  do                                                              \
    if (yychar == YYEMPTY)                                        \
      {                                                           \
        yychar = (Token);                                         \
        yylval = (Value);                                         \
        YYPOPSTACK (yylen);                                       \
        yystate = *yyssp;                                         \
        goto yybackup;                                            \
      }                                                           \
    else                                                          \
      {                                                           \
        yyerror (&yylloc, state, YY_("syntax error: cannot back up")); \
        YYERROR;                                                  \
      }                                                           \
  while (0)

/* Backward compatibility with an undocumented macro.
   Use YYerror or YYUNDEF. */
#define YYERRCODE YYUNDEF

/* YYLLOC_DEFAULT -- Set CURRENT to span from RHS[1] to RHS[N].
   If N is 0, then set CURRENT to the empty location which ends
   the previous symbol: RHS[0] (always defined).  */

#ifndef YYLLOC_DEFAULT
# define YYLLOC_DEFAULT(Current, Rhs, N)                                \
    do                                                                  \
      if (N)                                                            \
        {                                                               \
          (Current).first_line   = YYRHSLOC (Rhs, 1).first_line;        \
          (Current).first_column = YYRHSLOC (Rhs, 1).first_column;      \
          (Current).last_line    = YYRHSLOC (Rhs, N).last_line;         \
          (Current).last_column  = YYRHSLOC (Rhs, N).last_column;       \
        }                                                               \
      else                                                              \
        {                                                               \
          (Current).first_line   = (Current).last_line   =              \
            YYRHSLOC (Rhs, 0).last_line;                                \
          (Current).first_column = (Current).last_column =              \
            YYRHSLOC (Rhs, 0).last_column;                              \
        }                                                               \
    while (0)
#endif

#define YYRHSLOC(Rhs, K) ((Rhs)[K])


/* Enable debugging if requested.  */
#if YYDEBUG

# ifndef YYFPRINTF
#  include <stdio.h> /* INFRINGES ON USER NAME SPACE */
#  define YYFPRINTF fprintf
# endif

# define YYDPRINTF(Args)                        \
do {                                            \
  if (yydebug)                                  \
    YYFPRINTF Args;                             \
} while (0)


/* YYLOCATION_PRINT -- Print the location on the stream.
   This macro was not mandated originally: define only if we know
   we won't break user code: when these are the locations we know.  */

# ifndef YYLOCATION_PRINT

#  if defined YY_LOCATION_PRINT

   /* Temporary convenience wrapper in case some people defined the
      undocumented and private YY_LOCATION_PRINT macros.  */
#   define YYLOCATION_PRINT(File, Loc)  YY_LOCATION_PRINT(File, *(Loc))

#  elif defined YYLTYPE_IS_TRIVIAL && YYLTYPE_IS_TRIVIAL

/* Print *YYLOCP on YYO.  Private, do not rely on its existence. */

YY_ATTRIBUTE_UNUSED
static int
yy_location_print_ (FILE *yyo, YYLTYPE const * const yylocp)
{
  int res = 0;
  int end_col = 0 != yylocp->last_column ? yylocp->last_column - 1 : 0;
  if (0 <= yylocp->first_line)
    {
      res += YYFPRINTF (yyo, "%d", yylocp->first_line);
      if (0 <= yylocp->first_column)
        res += YYFPRINTF (yyo, ".%d", yylocp->first_column);
    }
  if (0 <= yylocp->last_line)
    {
      if (yylocp->first_line < yylocp->last_line)
        {
          res += YYFPRINTF (yyo, "-%d", yylocp->last_line);
          if (0 <= end_col)
            res += YYFPRINTF (yyo, ".%d", end_col);
        }
      else if (0 <= end_col && yylocp->first_column < end_col)
        res += YYFPRINTF (yyo, "-%d", end_col);
    }
  return res;
}

#   define YYLOCATION_PRINT  yy_location_print_

    /* Temporary convenience wrapper in case some people defined the
       undocumented and private YY_LOCATION_PRINT macros.  */
#   define YY_LOCATION_PRINT(File, Loc)  YYLOCATION_PRINT(File, &(Loc))

#  else

#   define YYLOCATION_PRINT(File, Loc) ((void) 0)
    /* Temporary convenience wrapper in case some people defined the
       undocumented and private YY_LOCATION_PRINT macros.  */
#   define YY_LOCATION_PRINT  YYLOCATION_PRINT

#  endif
# endif /* !defined YYLOCATION_PRINT */


# define YY_SYMBOL_PRINT(Title, Kind, Value, Location)                    \
do {                                                                      \
  if (yydebug)                                                            \
    {                                                                     \
      YYFPRINTF (stderr, "%s ", Title);                                   \
      yy_symbol_print (stderr,                                            \
                  Kind, Value, Location, state); \
      YYFPRINTF (stderr, "\n");                                           \
    }                                                                     \
} while (0)


/*-----------------------------------.
| Print this symbol's value on YYO.  |
`-----------------------------------*/

static void
yy_symbol_value_print (FILE *yyo,
                       yysymbol_kind_t yykind, YYSTYPE const * const yyvaluep, YYLTYPE const * const yylocationp, struct _mesa_glsl_parse_state *state)
{
  FILE *yyoutput = yyo;
  YY_USE (yyoutput);
  YY_USE (yylocationp);
  YY_USE (state);
  if (!yyvaluep)
    return;
  YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
  YY_USE (yykind);
  YY_IGNORE_MAYBE_UNINITIALIZED_END
}


/*---------------------------.
| Print this symbol on YYO.  |
`---------------------------*/

static void
yy_symbol_print (FILE *yyo,
                 yysymbol_kind_t yykind, YYSTYPE const * const yyvaluep, YYLTYPE const * const yylocationp, struct _mesa_glsl_parse_state *state)
{
  YYFPRINTF (yyo, "%s %s (",
             yykind < YYNTOKENS ? "token" : "nterm", yysymbol_name (yykind));

  YYLOCATION_PRINT (yyo, yylocationp);
  YYFPRINTF (yyo, ": ");
  yy_symbol_value_print (yyo, yykind, yyvaluep, yylocationp, state);
  YYFPRINTF (yyo, ")");
}

/*------------------------------------------------------------------.
| yy_stack_print -- Print the state stack from its BOTTOM up to its |
| TOP (included).                                                   |
`------------------------------------------------------------------*/

static void
yy_stack_print (yy_state_t *yybottom, yy_state_t *yytop)
{
  YYFPRINTF (stderr, "Stack now");
  for (; yybottom <= yytop; yybottom++)
    {
      int yybot = *yybottom;
      YYFPRINTF (stderr, " %d", yybot);
    }
  YYFPRINTF (stderr, "\n");
}

# define YY_STACK_PRINT(Bottom, Top)                            \
do {                                                            \
  if (yydebug)                                                  \
    yy_stack_print ((Bottom), (Top));                           \
} while (0)


/*------------------------------------------------.
| Report that the YYRULE is going to be reduced.  |
`------------------------------------------------*/

static void
yy_reduce_print (yy_state_t *yyssp, YYSTYPE *yyvsp, YYLTYPE *yylsp,
                 int yyrule, struct _mesa_glsl_parse_state *state)
{
  int yylno = yyrline[yyrule];
  int yynrhs = yyr2[yyrule];
  int yyi;
  YYFPRINTF (stderr, "Reducing stack by rule %d (line %d):\n",
             yyrule - 1, yylno);
  /* The symbols being reduced.  */
  for (yyi = 0; yyi < yynrhs; yyi++)
    {
      YYFPRINTF (stderr, "   $%d = ", yyi + 1);
      yy_symbol_print (stderr,
                       YY_ACCESSING_SYMBOL (+yyssp[yyi + 1 - yynrhs]),
                       &yyvsp[(yyi + 1) - (yynrhs)],
                       &(yylsp[(yyi + 1) - (yynrhs)]), state);
      YYFPRINTF (stderr, "\n");
    }
}

# define YY_REDUCE_PRINT(Rule)          \
do {                                    \
  if (yydebug)                          \
    yy_reduce_print (yyssp, yyvsp, yylsp, Rule, state); \
} while (0)

/* Nonzero means print parse trace.  It is left uninitialized so that
   multiple parsers can coexist.  */
int yydebug;
#else /* !YYDEBUG */
# define YYDPRINTF(Args) ((void) 0)
# define YY_SYMBOL_PRINT(Title, Kind, Value, Location)
# define YY_STACK_PRINT(Bottom, Top)
# define YY_REDUCE_PRINT(Rule)
#endif /* !YYDEBUG */


/* YYINITDEPTH -- initial size of the parser's stacks.  */
#ifndef YYINITDEPTH
# define YYINITDEPTH 200
#endif

/* YYMAXDEPTH -- maximum size the stacks can grow to (effective only
   if the built-in stack extension method is used).

   Do not make this value too large; the results are undefined if
   YYSTACK_ALLOC_MAXIMUM < YYSTACK_BYTES (YYMAXDEPTH)
   evaluated with infinite-precision integer arithmetic.  */

#ifndef YYMAXDEPTH
# define YYMAXDEPTH 10000
#endif


/* Context of a parse error.  */
typedef struct
{
  yy_state_t *yyssp;
  yysymbol_kind_t yytoken;
  YYLTYPE *yylloc;
} yypcontext_t;

/* Put in YYARG at most YYARGN of the expected tokens given the
   current YYCTX, and return the number of tokens stored in YYARG.  If
   YYARG is null, return the number of expected tokens (guaranteed to
   be less than YYNTOKENS).  Return YYENOMEM on memory exhaustion.
   Return 0 if there are more than YYARGN expected tokens, yet fill
   YYARG up to YYARGN. */
static int
yypcontext_expected_tokens (const yypcontext_t *yyctx,
                            yysymbol_kind_t yyarg[], int yyargn)
{
  /* Actual size of YYARG. */
  int yycount = 0;
  int yyn = yypact[+*yyctx->yyssp];
  if (!yypact_value_is_default (yyn))
    {
      /* Start YYX at -YYN if negative to avoid negative indexes in
         YYCHECK.  In other words, skip the first -YYN actions for
         this state because they are default actions.  */
      int yyxbegin = yyn < 0 ? -yyn : 0;
      /* Stay within bounds of both yycheck and yytname.  */
      int yychecklim = YYLAST - yyn + 1;
      int yyxend = yychecklim < YYNTOKENS ? yychecklim : YYNTOKENS;
      int yyx;
      for (yyx = yyxbegin; yyx < yyxend; ++yyx)
        if (yycheck[yyx + yyn] == yyx && yyx != YYSYMBOL_YYerror
            && !yytable_value_is_error (yytable[yyx + yyn]))
          {
            if (!yyarg)
              ++yycount;
            else if (yycount == yyargn)
              return 0;
            else
              yyarg[yycount++] = YY_CAST (yysymbol_kind_t, yyx);
          }
    }
  if (yyarg && yycount == 0 && 0 < yyargn)
    yyarg[0] = YYSYMBOL_YYEMPTY;
  return yycount;
}




#ifndef yystrlen
# if defined __GLIBC__ && defined _STRING_H
#  define yystrlen(S) (YY_CAST (YYPTRDIFF_T, strlen (S)))
# else
/* Return the length of YYSTR.  */
static YYPTRDIFF_T
yystrlen (const char *yystr)
{
  YYPTRDIFF_T yylen;
  for (yylen = 0; yystr[yylen]; yylen++)
    continue;
  return yylen;
}
# endif
#endif

#ifndef yystpcpy
# if defined __GLIBC__ && defined _STRING_H && defined _GNU_SOURCE
#  define yystpcpy stpcpy
# else
/* Copy YYSRC to YYDEST, returning the address of the terminating '\0' in
   YYDEST.  */
static char *
yystpcpy (char *yydest, const char *yysrc)
{
  char *yyd = yydest;
  const char *yys = yysrc;

  while ((*yyd++ = *yys++) != '\0')
    continue;

  return yyd - 1;
}
# endif
#endif

#ifndef yytnamerr
/* Copy to YYRES the contents of YYSTR after stripping away unnecessary
   quotes and backslashes, so that it's suitable for yyerror.  The
   heuristic is that double-quoting is unnecessary unless the string
   contains an apostrophe, a comma, or backslash (other than
   backslash-backslash).  YYSTR is taken from yytname.  If YYRES is
   null, do not copy; instead, return the length of what the result
   would have been.  */
static YYPTRDIFF_T
yytnamerr (char *yyres, const char *yystr)
{
  if (*yystr == '"')
    {
      YYPTRDIFF_T yyn = 0;
      char const *yyp = yystr;
      for (;;)
        switch (*++yyp)
          {
          case '\'':
          case ',':
            goto do_not_strip_quotes;

          case '\\':
            if (*++yyp != '\\')
              goto do_not_strip_quotes;
            else
              goto append;

          append:
          default:
            if (yyres)
              yyres[yyn] = *yyp;
            yyn++;
            break;

          case '"':
            if (yyres)
              yyres[yyn] = '\0';
            return yyn;
          }
    do_not_strip_quotes: ;
    }

  if (yyres)
    return yystpcpy (yyres, yystr) - yyres;
  else
    return yystrlen (yystr);
}
#endif


static int
yy_syntax_error_arguments (const yypcontext_t *yyctx,
                           yysymbol_kind_t yyarg[], int yyargn)
{
  /* Actual size of YYARG. */
  int yycount = 0;
  /* There are many possibilities here to consider:
     - If this state is a consistent state with a default action, then
       the only way this function was invoked is if the default action
       is an error action.  In that case, don't check for expected
       tokens because there are none.
     - The only way there can be no lookahead present (in yychar) is if
       this state is a consistent state with a default action.  Thus,
       detecting the absence of a lookahead is sufficient to determine
       that there is no unexpected or expected token to report.  In that
       case, just report a simple "syntax error".
     - Don't assume there isn't a lookahead just because this state is a
       consistent state with a default action.  There might have been a
       previous inconsistent state, consistent state with a non-default
       action, or user semantic action that manipulated yychar.
     - Of course, the expected token list depends on states to have
       correct lookahead information, and it depends on the parser not
       to perform extra reductions after fetching a lookahead from the
       scanner and before detecting a syntax error.  Thus, state merging
       (from LALR or IELR) and default reductions corrupt the expected
       token list.  However, the list is correct for canonical LR with
       one exception: it will still contain any token that will not be
       accepted due to an error action in a later state.
  */
  if (yyctx->yytoken != YYSYMBOL_YYEMPTY)
    {
      int yyn;
      if (yyarg)
        yyarg[yycount] = yyctx->yytoken;
      ++yycount;
      yyn = yypcontext_expected_tokens (yyctx,
                                        yyarg ? yyarg + 1 : yyarg, yyargn - 1);
      if (yyn == YYENOMEM)
        return YYENOMEM;
      else
        yycount += yyn;
    }
  return yycount;
}

/* Copy into *YYMSG, which is of size *YYMSG_ALLOC, an error message
   about the unexpected token YYTOKEN for the state stack whose top is
   YYSSP.

   Return 0 if *YYMSG was successfully written.  Return -1 if *YYMSG is
   not large enough to hold the message.  In that case, also set
   *YYMSG_ALLOC to the required number of bytes.  Return YYENOMEM if the
   required number of bytes is too large to store.  */
static int
yysyntax_error (YYPTRDIFF_T *yymsg_alloc, char **yymsg,
                const yypcontext_t *yyctx)
{
  enum { YYARGS_MAX = 5 };
  /* Internationalized format string. */
  const char *yyformat = YY_NULLPTR;
  /* Arguments of yyformat: reported tokens (one for the "unexpected",
     one per "expected"). */
  yysymbol_kind_t yyarg[YYARGS_MAX];
  /* Cumulated lengths of YYARG.  */
  YYPTRDIFF_T yysize = 0;

  /* Actual size of YYARG. */
  int yycount = yy_syntax_error_arguments (yyctx, yyarg, YYARGS_MAX);
  if (yycount == YYENOMEM)
    return YYENOMEM;

  switch (yycount)
    {
#define YYCASE_(N, S)                       \
      case N:                               \
        yyformat = S;                       \
        break
    default: /* Avoid compiler warnings. */
      YYCASE_(0, YY_("syntax error"));
      YYCASE_(1, YY_("syntax error, unexpected %s"));
      YYCASE_(2, YY_("syntax error, unexpected %s, expecting %s"));
      YYCASE_(3, YY_("syntax error, unexpected %s, expecting %s or %s"));
      YYCASE_(4, YY_("syntax error, unexpected %s, expecting %s or %s or %s"));
      YYCASE_(5, YY_("syntax error, unexpected %s, expecting %s or %s or %s or %s"));
#undef YYCASE_
    }

  /* Compute error message size.  Don't count the "%s"s, but reserve
     room for the terminator.  */
  yysize = yystrlen (yyformat) - 2 * yycount + 1;
  {
    int yyi;
    for (yyi = 0; yyi < yycount; ++yyi)
      {
        YYPTRDIFF_T yysize1
          = yysize + yytnamerr (YY_NULLPTR, yytname[yyarg[yyi]]);
        if (yysize <= yysize1 && yysize1 <= YYSTACK_ALLOC_MAXIMUM)
          yysize = yysize1;
        else
          return YYENOMEM;
      }
  }

  if (*yymsg_alloc < yysize)
    {
      *yymsg_alloc = 2 * yysize;
      if (! (yysize <= *yymsg_alloc
             && *yymsg_alloc <= YYSTACK_ALLOC_MAXIMUM))
        *yymsg_alloc = YYSTACK_ALLOC_MAXIMUM;
      return -1;
    }

  /* Avoid sprintf, as that infringes on the user's name space.
     Don't have undefined behavior even if the translation
     produced a string with the wrong number of "%s"s.  */
  {
    char *yyp = *yymsg;
    int yyi = 0;
    while ((*yyp = *yyformat) != '\0')
      if (*yyp == '%' && yyformat[1] == 's' && yyi < yycount)
        {
          yyp += yytnamerr (yyp, yytname[yyarg[yyi++]]);
          yyformat += 2;
        }
      else
        {
          ++yyp;
          ++yyformat;
        }
  }
  return 0;
}


/*-----------------------------------------------.
| Release the memory associated to this symbol.  |
`-----------------------------------------------*/

static void
yydestruct (const char *yymsg,
            yysymbol_kind_t yykind, YYSTYPE *yyvaluep, YYLTYPE *yylocationp, struct _mesa_glsl_parse_state *state)
{
  YY_USE (yyvaluep);
  YY_USE (yylocationp);
  YY_USE (state);
  if (!yymsg)
    yymsg = "Deleting";
  YY_SYMBOL_PRINT (yymsg, yykind, yyvaluep, yylocationp);

  YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
  YY_USE (yykind);
  YY_IGNORE_MAYBE_UNINITIALIZED_END
}






/*----------.
| yyparse.  |
`----------*/

int
yyparse (struct _mesa_glsl_parse_state *state)
{
/* Lookahead token kind.  */
int yychar;


/* The semantic value of the lookahead symbol.  */
/* Default value used for initialization, for pacifying older GCCs
   or non-GCC compilers.  */
YY_INITIAL_VALUE (static YYSTYPE yyval_default;)
YYSTYPE yylval YY_INITIAL_VALUE (= yyval_default);

/* Location data for the lookahead symbol.  */
static YYLTYPE yyloc_default
# if defined YYLTYPE_IS_TRIVIAL && YYLTYPE_IS_TRIVIAL
  = { 1, 1, 1, 1 }
# endif
;
YYLTYPE yylloc = yyloc_default;

    /* Number of syntax errors so far.  */
    int yynerrs = 0;

    yy_state_fast_t yystate = 0;
    /* Number of tokens to shift before error messages enabled.  */
    int yyerrstatus = 0;

    /* Refer to the stacks through separate pointers, to allow yyoverflow
       to reallocate them elsewhere.  */

    /* Their size.  */
    YYPTRDIFF_T yystacksize = YYINITDEPTH;

    /* The state stack: array, bottom, top.  */
    yy_state_t yyssa[YYINITDEPTH];
    yy_state_t *yyss = yyssa;
    yy_state_t *yyssp = yyss;

    /* The semantic value stack: array, bottom, top.  */
    YYSTYPE yyvsa[YYINITDEPTH];
    YYSTYPE *yyvs = yyvsa;
    YYSTYPE *yyvsp = yyvs;

    /* The location stack: array, bottom, top.  */
    YYLTYPE yylsa[YYINITDEPTH];
    YYLTYPE *yyls = yylsa;
    YYLTYPE *yylsp = yyls;

  int yyn;
  /* The return value of yyparse.  */
  int yyresult;
  /* Lookahead symbol kind.  */
  yysymbol_kind_t yytoken = YYSYMBOL_YYEMPTY;
  /* The variables used to return semantic value and location from the
     action routines.  */
  YYSTYPE yyval;
  YYLTYPE yyloc;

  /* The locations where the error started and ended.  */
  YYLTYPE yyerror_range[3];

  /* Buffer for error messages, and its allocated size.  */
  char yymsgbuf[128];
  char *yymsg = yymsgbuf;
  YYPTRDIFF_T yymsg_alloc = sizeof yymsgbuf;

#define YYPOPSTACK(N)   (yyvsp -= (N), yyssp -= (N), yylsp -= (N))

  /* The number of symbols on the RHS of the reduced rule.
     Keep to zero when no symbol should be popped.  */
  int yylen = 0;

  YYDPRINTF ((stderr, "Starting parse\n"));

  yychar = YYEMPTY; /* Cause a token to be read.  */


/* User initialization code.  */
#line 89 "../src/compiler/glsl/glsl_parser.yy"
{
   yylloc.first_line = 1;
   yylloc.first_column = 1;
   yylloc.last_line = 1;
   yylloc.last_column = 1;
   yylloc.source = 0;
   yylloc.path = NULL;
}

#line 2439 "src/compiler/glsl/glsl_parser.cpp"

  yylsp[0] = yylloc;
  goto yysetstate;


/*------------------------------------------------------------.
| yynewstate -- push a new state, which is found in yystate.  |
`------------------------------------------------------------*/
yynewstate:
  /* In all cases, when you get here, the value and location stacks
     have just been pushed.  So pushing a state here evens the stacks.  */
  yyssp++;


/*--------------------------------------------------------------------.
| yysetstate -- set current state (the top of the stack) to yystate.  |
`--------------------------------------------------------------------*/
yysetstate:
  YYDPRINTF ((stderr, "Entering state %d\n", yystate));
  YY_ASSERT (0 <= yystate && yystate < YYNSTATES);
  YY_IGNORE_USELESS_CAST_BEGIN
  *yyssp = YY_CAST (yy_state_t, yystate);
  YY_IGNORE_USELESS_CAST_END
  YY_STACK_PRINT (yyss, yyssp);

  if (yyss + yystacksize - 1 <= yyssp)
#if !defined yyoverflow && !defined YYSTACK_RELOCATE
    YYNOMEM;
#else
    {
      /* Get the current used size of the three stacks, in elements.  */
      YYPTRDIFF_T yysize = yyssp - yyss + 1;

# if defined yyoverflow
      {
        /* Give user a chance to reallocate the stack.  Use copies of
           these so that the &'s don't force the real ones into
           memory.  */
        yy_state_t *yyss1 = yyss;
        YYSTYPE *yyvs1 = yyvs;
        YYLTYPE *yyls1 = yyls;

        /* Each stack pointer address is followed by the size of the
           data in use in that stack, in bytes.  This used to be a
           conditional around just the two extra args, but that might
           be undefined if yyoverflow is a macro.  */
        yyoverflow (YY_("memory exhausted"),
                    &yyss1, yysize * YYSIZEOF (*yyssp),
                    &yyvs1, yysize * YYSIZEOF (*yyvsp),
                    &yyls1, yysize * YYSIZEOF (*yylsp),
                    &yystacksize);
        yyss = yyss1;
        yyvs = yyvs1;
        yyls = yyls1;
      }
# else /* defined YYSTACK_RELOCATE */
      /* Extend the stack our own way.  */
      if (YYMAXDEPTH <= yystacksize)
        YYNOMEM;
      yystacksize *= 2;
      if (YYMAXDEPTH < yystacksize)
        yystacksize = YYMAXDEPTH;

      {
        yy_state_t *yyss1 = yyss;
        union yyalloc *yyptr =
          YY_CAST (union yyalloc *,
                   YYSTACK_ALLOC (YY_CAST (YYSIZE_T, YYSTACK_BYTES (yystacksize))));
        if (! yyptr)
          YYNOMEM;
        YYSTACK_RELOCATE (yyss_alloc, yyss);
        YYSTACK_RELOCATE (yyvs_alloc, yyvs);
        YYSTACK_RELOCATE (yyls_alloc, yyls);
#  undef YYSTACK_RELOCATE
        if (yyss1 != yyssa)
          YYSTACK_FREE (yyss1);
      }
# endif

      yyssp = yyss + yysize - 1;
      yyvsp = yyvs + yysize - 1;
      yylsp = yyls + yysize - 1;

      YY_IGNORE_USELESS_CAST_BEGIN
      YYDPRINTF ((stderr, "Stack size increased to %ld\n",
                  YY_CAST (long, yystacksize)));
      YY_IGNORE_USELESS_CAST_END

      if (yyss + yystacksize - 1 <= yyssp)
        YYABORT;
    }
#endif /* !defined yyoverflow && !defined YYSTACK_RELOCATE */


  if (yystate == YYFINAL)
    YYACCEPT;

  goto yybackup;


/*-----------.
| yybackup.  |
`-----------*/
yybackup:
  /* Do appropriate processing given the current state.  Read a
     lookahead token if we need one and don't already have one.  */

  /* First try to decide what to do without reference to lookahead token.  */
  yyn = yypact[yystate];
  if (yypact_value_is_default (yyn))
    goto yydefault;

  /* Not known => get a lookahead token if don't already have one.  */

  /* YYCHAR is either empty, or end-of-input, or a valid lookahead.  */
  if (yychar == YYEMPTY)
    {
      YYDPRINTF ((stderr, "Reading a token\n"));
      yychar = yylex (&yylval, &yylloc, state);
    }

  if (yychar <= YYEOF)
    {
      yychar = YYEOF;
      yytoken = YYSYMBOL_YYEOF;
      YYDPRINTF ((stderr, "Now at end of input.\n"));
    }
  else if (yychar == YYerror)
    {
      /* The scanner already issued an error message, process directly
         to error recovery.  But do not keep the error token as
         lookahead, it is too special and may lead us to an endless
         loop in error recovery. */
      yychar = YYUNDEF;
      yytoken = YYSYMBOL_YYerror;
      yyerror_range[1] = yylloc;
      goto yyerrlab1;
    }
  else
    {
      yytoken = YYTRANSLATE (yychar);
      YY_SYMBOL_PRINT ("Next token is", yytoken, &yylval, &yylloc);
    }

  /* If the proper action on seeing token YYTOKEN is to reduce or to
     detect an error, take that action.  */
  yyn += yytoken;
  if (yyn < 0 || YYLAST < yyn || yycheck[yyn] != yytoken)
    goto yydefault;
  yyn = yytable[yyn];
  if (yyn <= 0)
    {
      if (yytable_value_is_error (yyn))
        goto yyerrlab;
      yyn = -yyn;
      goto yyreduce;
    }

  /* Count tokens shifted since error; after three, turn off error
     status.  */
  if (yyerrstatus)
    yyerrstatus--;

  /* Shift the lookahead token.  */
  YY_SYMBOL_PRINT ("Shifting", yytoken, &yylval, &yylloc);
  yystate = yyn;
  YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
  *++yyvsp = yylval;
  YY_IGNORE_MAYBE_UNINITIALIZED_END
  *++yylsp = yylloc;

  /* Discard the shifted token.  */
  yychar = YYEMPTY;
  goto yynewstate;


/*-----------------------------------------------------------.
| yydefault -- do the default action for the current state.  |
`-----------------------------------------------------------*/
yydefault:
  yyn = yydefact[yystate];
  if (yyn == 0)
    goto yyerrlab;
  goto yyreduce;


/*-----------------------------.
| yyreduce -- do a reduction.  |
`-----------------------------*/
yyreduce:
  /* yyn is the number of a rule to reduce with.  */
  yylen = yyr2[yyn];

  /* If YYLEN is nonzero, implement the default value of the action:
     '$$ = $1'.

     Otherwise, the following line sets YYVAL to garbage.
     This behavior is undocumented and Bison
     users should not rely upon it.  Assigning to YYVAL
     unconditionally makes the parser a bit smaller, and it avoids a
     GCC warning that YYVAL may be used uninitialized.  */
  yyval = yyvsp[1-yylen];

  /* Default location. */
  YYLLOC_DEFAULT (yyloc, (yylsp - yylen), yylen);
  yyerror_range[1] = yyloc;
  YY_REDUCE_PRINT (yyn);
  switch (yyn)
    {
  case 2: /* $@1: %empty  */
#line 295 "../src/compiler/glsl/glsl_parser.yy"
   {
      _mesa_glsl_initialize_types(state);
   }
#line 2654 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 3: /* translation_unit: version_statement extension_statement_list $@1 external_declaration_list  */
#line 299 "../src/compiler/glsl/glsl_parser.yy"
   {
      delete state->symbols;
      state->symbols = new(ralloc_parent(state)) glsl_symbol_table;
      if (state->es_shader) {
         if (state->stage == MESA_SHADER_FRAGMENT) {
            state->symbols->add_default_precision_qualifier("int", ast_precision_medium);
         } else {
            state->symbols->add_default_precision_qualifier("float", ast_precision_high);
            state->symbols->add_default_precision_qualifier("int", ast_precision_high);
         }
         state->symbols->add_default_precision_qualifier("sampler2D", ast_precision_low);
         state->symbols->add_default_precision_qualifier("samplerExternalOES", ast_precision_low);
         state->symbols->add_default_precision_qualifier("samplerCube", ast_precision_low);
         state->symbols->add_default_precision_qualifier("atomic_uint", ast_precision_high);
      }
      _mesa_glsl_initialize_types(state);
   }
#line 2676 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 5: /* version_statement: VERSION_TOK INTCONSTANT EOL  */
#line 321 "../src/compiler/glsl/glsl_parser.yy"
   {
      state->process_version_directive(&(yylsp[-1]), (yyvsp[-1].n), NULL);
      if (state->error) {
         YYERROR;
      }
   }
#line 2687 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 6: /* version_statement: VERSION_TOK INTCONSTANT any_identifier EOL  */
#line 328 "../src/compiler/glsl/glsl_parser.yy"
   {
      state->process_version_directive(&(yylsp[-2]), (yyvsp[-2].n), (yyvsp[-1].identifier));
      if (state->error) {
         YYERROR;
      }
   }
#line 2698 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 7: /* pragma_statement: PRAGMA_DEBUG_ON EOL  */
#line 337 "../src/compiler/glsl/glsl_parser.yy"
                       { (yyval.node) = NULL; }
#line 2704 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 8: /* pragma_statement: PRAGMA_DEBUG_OFF EOL  */
#line 338 "../src/compiler/glsl/glsl_parser.yy"
                          { (yyval.node) = NULL; }
#line 2710 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 9: /* pragma_statement: PRAGMA_OPTIMIZE_ON EOL  */
#line 339 "../src/compiler/glsl/glsl_parser.yy"
                            { (yyval.node) = NULL; }
#line 2716 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 10: /* pragma_statement: PRAGMA_OPTIMIZE_OFF EOL  */
#line 340 "../src/compiler/glsl/glsl_parser.yy"
                             { (yyval.node) = NULL; }
#line 2722 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 11: /* pragma_statement: PRAGMA_INVARIANT_ALL EOL  */
#line 342 "../src/compiler/glsl/glsl_parser.yy"
   {
      /* Pragma invariant(all) cannot be used in a fragment shader.
       *
       * Page 27 of the GLSL 1.20 spec, Page 53 of the GLSL ES 3.00 spec:
       *
       *     "It is an error to use this pragma in a fragment shader."
       */
      if (state->is_version(120, 300) &&
          state->stage == MESA_SHADER_FRAGMENT) {
         _mesa_glsl_error(& (yylsp[-1]), state,
                          "pragma `invariant(all)' cannot be used "
                          "in a fragment shader.");
      } else if (!state->is_version(120, 100)) {
         _mesa_glsl_warning(& (yylsp[-1]), state,
                            "pragma `invariant(all)' not supported in %s "
                            "(GLSL ES 1.00 or GLSL 1.20 required)",
                            state->get_version_string());
      } else {
         state->all_invariant = true;
      }

      (yyval.node) = NULL;
   }
#line 2750 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 12: /* pragma_statement: PRAGMA_WARNING_ON EOL  */
#line 366 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *mem_ctx = state->linalloc;
      (yyval.node) = new(mem_ctx) ast_warnings_toggle(true);
   }
#line 2759 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 13: /* pragma_statement: PRAGMA_WARNING_OFF EOL  */
#line 371 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *mem_ctx = state->linalloc;
      (yyval.node) = new(mem_ctx) ast_warnings_toggle(false);
   }
#line 2768 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 19: /* extension_statement: EXTENSION any_identifier COLON any_identifier EOL  */
#line 390 "../src/compiler/glsl/glsl_parser.yy"
   {
      if (!_mesa_glsl_process_extension((yyvsp[-3].identifier), & (yylsp[-3]), (yyvsp[-1].identifier), & (yylsp[-1]), state)) {
         YYERROR;
      }
   }
#line 2778 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 20: /* external_declaration_list: external_declaration  */
#line 399 "../src/compiler/glsl/glsl_parser.yy"
   {
      /* FINISHME: The NULL test is required because pragmas are set to
       * FINISHME: NULL. (See production rule for external_declaration.)
       */
      if ((yyvsp[0].node) != NULL)
         state->translation_unit.push_tail(& (yyvsp[0].node)->link);
   }
#line 2790 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 21: /* external_declaration_list: external_declaration_list external_declaration  */
#line 407 "../src/compiler/glsl/glsl_parser.yy"
   {
      /* FINISHME: The NULL test is required because pragmas are set to
       * FINISHME: NULL. (See production rule for external_declaration.)
       */
      if ((yyvsp[0].node) != NULL)
         state->translation_unit.push_tail(& (yyvsp[0].node)->link);
   }
#line 2802 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 22: /* external_declaration_list: external_declaration_list extension_statement  */
#line 414 "../src/compiler/glsl/glsl_parser.yy"
                                                   {
      if (!state->allow_extension_directive_midshader) {
         _mesa_glsl_error(& (yylsp[0]), state,
                          "#extension directive is not allowed "
                          "in the middle of a shader");
         YYERROR;
      }
   }
#line 2815 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 25: /* primary_expression: variable_identifier  */
#line 431 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression(ast_identifier, NULL, NULL, NULL);
      (yyval.expression)->set_location((yylsp[0]));
      (yyval.expression)->primary_expression.identifier = (yyvsp[0].identifier);
   }
#line 2826 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 26: /* primary_expression: INTCONSTANT  */
#line 438 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression(ast_int_constant, NULL, NULL, NULL);
      (yyval.expression)->set_location((yylsp[0]));
      (yyval.expression)->primary_expression.int_constant = (yyvsp[0].n);
   }
#line 2837 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 27: /* primary_expression: UINTCONSTANT  */
#line 445 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression(ast_uint_constant, NULL, NULL, NULL);
      (yyval.expression)->set_location((yylsp[0]));
      (yyval.expression)->primary_expression.uint_constant = (yyvsp[0].n);
   }
#line 2848 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 28: /* primary_expression: INT64CONSTANT  */
#line 452 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression(ast_int64_constant, NULL, NULL, NULL);
      (yyval.expression)->set_location((yylsp[0]));
      (yyval.expression)->primary_expression.int64_constant = (yyvsp[0].n64);
   }
#line 2859 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 29: /* primary_expression: UINT64CONSTANT  */
#line 459 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression(ast_uint64_constant, NULL, NULL, NULL);
      (yyval.expression)->set_location((yylsp[0]));
      (yyval.expression)->primary_expression.uint64_constant = (yyvsp[0].n64);
   }
#line 2870 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 30: /* primary_expression: FLOATCONSTANT  */
#line 466 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression(ast_float_constant, NULL, NULL, NULL);
      (yyval.expression)->set_location((yylsp[0]));
      (yyval.expression)->primary_expression.float_constant = (yyvsp[0].real);
   }
#line 2881 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 31: /* primary_expression: DOUBLECONSTANT  */
#line 473 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression(ast_double_constant, NULL, NULL, NULL);
      (yyval.expression)->set_location((yylsp[0]));
      (yyval.expression)->primary_expression.double_constant = (yyvsp[0].dreal);
   }
#line 2892 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 32: /* primary_expression: BOOLCONSTANT  */
#line 480 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression(ast_bool_constant, NULL, NULL, NULL);
      (yyval.expression)->set_location((yylsp[0]));
      (yyval.expression)->primary_expression.bool_constant = (yyvsp[0].n);
   }
#line 2903 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 33: /* primary_expression: '(' expression ')'  */
#line 487 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.expression) = (yyvsp[-1].expression);
   }
#line 2911 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 35: /* postfix_expression: postfix_expression '[' integer_expression ']'  */
#line 495 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression(ast_array_index, (yyvsp[-3].expression), (yyvsp[-1].expression), NULL);
      (yyval.expression)->set_location_range((yylsp[-3]), (yylsp[0]));
   }
#line 2921 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 36: /* postfix_expression: function_call  */
#line 501 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.expression) = (yyvsp[0].expression);
   }
#line 2929 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 37: /* postfix_expression: postfix_expression DOT_TOK FIELD_SELECTION  */
#line 505 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression(ast_field_selection, (yyvsp[-2].expression), NULL, NULL);
      (yyval.expression)->set_location_range((yylsp[-2]), (yylsp[0]));
      (yyval.expression)->primary_expression.identifier = (yyvsp[0].identifier);
   }
#line 2940 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 38: /* postfix_expression: postfix_expression INC_OP  */
#line 512 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression(ast_post_inc, (yyvsp[-1].expression), NULL, NULL);
      (yyval.expression)->set_location_range((yylsp[-1]), (yylsp[0]));
   }
#line 2950 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 39: /* postfix_expression: postfix_expression DEC_OP  */
#line 518 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression(ast_post_dec, (yyvsp[-1].expression), NULL, NULL);
      (yyval.expression)->set_location_range((yylsp[-1]), (yylsp[0]));
   }
#line 2960 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 47: /* function_call_header_with_parameters: function_call_header assignment_expression  */
#line 549 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.expression) = (yyvsp[-1].expression);
      (yyval.expression)->set_location((yylsp[-1]));
      (yyval.expression)->expressions.push_tail(& (yyvsp[0].expression)->link);
   }
#line 2970 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 48: /* function_call_header_with_parameters: function_call_header_with_parameters ',' assignment_expression  */
#line 555 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.expression) = (yyvsp[-2].expression);
      (yyval.expression)->set_location((yylsp[-2]));
      (yyval.expression)->expressions.push_tail(& (yyvsp[0].expression)->link);
   }
#line 2980 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 50: /* function_identifier: type_specifier  */
#line 571 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_function_expression((yyvsp[0].type_specifier));
      (yyval.expression)->set_location((yylsp[0]));
      }
#line 2990 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 51: /* function_identifier: postfix_expression  */
#line 577 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_function_expression((yyvsp[0].expression));
      (yyval.expression)->set_location((yylsp[0]));
      }
#line 3000 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 53: /* unary_expression: INC_OP unary_expression  */
#line 592 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression(ast_pre_inc, (yyvsp[0].expression), NULL, NULL);
      (yyval.expression)->set_location((yylsp[-1]));
   }
#line 3010 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 54: /* unary_expression: DEC_OP unary_expression  */
#line 598 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression(ast_pre_dec, (yyvsp[0].expression), NULL, NULL);
      (yyval.expression)->set_location((yylsp[-1]));
   }
#line 3020 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 55: /* unary_expression: unary_operator unary_expression  */
#line 604 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression((yyvsp[-1].n), (yyvsp[0].expression), NULL, NULL);
      (yyval.expression)->set_location_range((yylsp[-1]), (yylsp[0]));
   }
#line 3030 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 56: /* unary_operator: '+'  */
#line 613 "../src/compiler/glsl/glsl_parser.yy"
         { (yyval.n) = ast_plus; }
#line 3036 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 57: /* unary_operator: '-'  */
#line 614 "../src/compiler/glsl/glsl_parser.yy"
         { (yyval.n) = ast_neg; }
#line 3042 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 58: /* unary_operator: '!'  */
#line 615 "../src/compiler/glsl/glsl_parser.yy"
         { (yyval.n) = ast_logic_not; }
#line 3048 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 59: /* unary_operator: '~'  */
#line 616 "../src/compiler/glsl/glsl_parser.yy"
         { (yyval.n) = ast_bit_not; }
#line 3054 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 61: /* multiplicative_expression: multiplicative_expression '*' unary_expression  */
#line 622 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression_bin(ast_mul, (yyvsp[-2].expression), (yyvsp[0].expression));
      (yyval.expression)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 3064 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 62: /* multiplicative_expression: multiplicative_expression '/' unary_expression  */
#line 628 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression_bin(ast_div, (yyvsp[-2].expression), (yyvsp[0].expression));
      (yyval.expression)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 3074 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 63: /* multiplicative_expression: multiplicative_expression '%' unary_expression  */
#line 634 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression_bin(ast_mod, (yyvsp[-2].expression), (yyvsp[0].expression));
      (yyval.expression)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 3084 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 65: /* additive_expression: additive_expression '+' multiplicative_expression  */
#line 644 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression_bin(ast_add, (yyvsp[-2].expression), (yyvsp[0].expression));
      (yyval.expression)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 3094 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 66: /* additive_expression: additive_expression '-' multiplicative_expression  */
#line 650 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression_bin(ast_sub, (yyvsp[-2].expression), (yyvsp[0].expression));
      (yyval.expression)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 3104 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 68: /* shift_expression: shift_expression LEFT_OP additive_expression  */
#line 660 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression_bin(ast_lshift, (yyvsp[-2].expression), (yyvsp[0].expression));
      (yyval.expression)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 3114 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 69: /* shift_expression: shift_expression RIGHT_OP additive_expression  */
#line 666 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression_bin(ast_rshift, (yyvsp[-2].expression), (yyvsp[0].expression));
      (yyval.expression)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 3124 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 71: /* relational_expression: relational_expression '<' shift_expression  */
#line 676 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression_bin(ast_less, (yyvsp[-2].expression), (yyvsp[0].expression));
      (yyval.expression)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 3134 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 72: /* relational_expression: relational_expression '>' shift_expression  */
#line 682 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression_bin(ast_greater, (yyvsp[-2].expression), (yyvsp[0].expression));
      (yyval.expression)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 3144 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 73: /* relational_expression: relational_expression LE_OP shift_expression  */
#line 688 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression_bin(ast_lequal, (yyvsp[-2].expression), (yyvsp[0].expression));
      (yyval.expression)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 3154 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 74: /* relational_expression: relational_expression GE_OP shift_expression  */
#line 694 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression_bin(ast_gequal, (yyvsp[-2].expression), (yyvsp[0].expression));
      (yyval.expression)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 3164 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 76: /* equality_expression: equality_expression EQ_OP relational_expression  */
#line 704 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression_bin(ast_equal, (yyvsp[-2].expression), (yyvsp[0].expression));
      (yyval.expression)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 3174 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 77: /* equality_expression: equality_expression NE_OP relational_expression  */
#line 710 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression_bin(ast_nequal, (yyvsp[-2].expression), (yyvsp[0].expression));
      (yyval.expression)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 3184 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 79: /* and_expression: and_expression '&' equality_expression  */
#line 720 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression_bin(ast_bit_and, (yyvsp[-2].expression), (yyvsp[0].expression));
      (yyval.expression)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 3194 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 81: /* exclusive_or_expression: exclusive_or_expression '^' and_expression  */
#line 730 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression_bin(ast_bit_xor, (yyvsp[-2].expression), (yyvsp[0].expression));
      (yyval.expression)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 3204 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 83: /* inclusive_or_expression: inclusive_or_expression '|' exclusive_or_expression  */
#line 740 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression_bin(ast_bit_or, (yyvsp[-2].expression), (yyvsp[0].expression));
      (yyval.expression)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 3214 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 85: /* logical_and_expression: logical_and_expression AND_OP inclusive_or_expression  */
#line 750 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression_bin(ast_logic_and, (yyvsp[-2].expression), (yyvsp[0].expression));
      (yyval.expression)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 3224 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 87: /* logical_xor_expression: logical_xor_expression XOR_OP logical_and_expression  */
#line 760 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression_bin(ast_logic_xor, (yyvsp[-2].expression), (yyvsp[0].expression));
      (yyval.expression)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 3234 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 89: /* logical_or_expression: logical_or_expression OR_OP logical_xor_expression  */
#line 770 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression_bin(ast_logic_or, (yyvsp[-2].expression), (yyvsp[0].expression));
      (yyval.expression)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 3244 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 91: /* conditional_expression: logical_or_expression '?' expression ':' assignment_expression  */
#line 780 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression(ast_conditional, (yyvsp[-4].expression), (yyvsp[-2].expression), (yyvsp[0].expression));
      (yyval.expression)->set_location_range((yylsp[-4]), (yylsp[0]));
   }
#line 3254 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 93: /* assignment_expression: unary_expression assignment_operator assignment_expression  */
#line 790 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_expression((yyvsp[-1].n), (yyvsp[-2].expression), (yyvsp[0].expression), NULL);
      (yyval.expression)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 3264 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 94: /* assignment_operator: '='  */
#line 798 "../src/compiler/glsl/glsl_parser.yy"
                      { (yyval.n) = ast_assign; }
#line 3270 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 95: /* assignment_operator: MUL_ASSIGN  */
#line 799 "../src/compiler/glsl/glsl_parser.yy"
                      { (yyval.n) = ast_mul_assign; }
#line 3276 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 96: /* assignment_operator: DIV_ASSIGN  */
#line 800 "../src/compiler/glsl/glsl_parser.yy"
                      { (yyval.n) = ast_div_assign; }
#line 3282 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 97: /* assignment_operator: MOD_ASSIGN  */
#line 801 "../src/compiler/glsl/glsl_parser.yy"
                      { (yyval.n) = ast_mod_assign; }
#line 3288 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 98: /* assignment_operator: ADD_ASSIGN  */
#line 802 "../src/compiler/glsl/glsl_parser.yy"
                      { (yyval.n) = ast_add_assign; }
#line 3294 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 99: /* assignment_operator: SUB_ASSIGN  */
#line 803 "../src/compiler/glsl/glsl_parser.yy"
                      { (yyval.n) = ast_sub_assign; }
#line 3300 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 100: /* assignment_operator: LEFT_ASSIGN  */
#line 804 "../src/compiler/glsl/glsl_parser.yy"
                      { (yyval.n) = ast_ls_assign; }
#line 3306 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 101: /* assignment_operator: RIGHT_ASSIGN  */
#line 805 "../src/compiler/glsl/glsl_parser.yy"
                      { (yyval.n) = ast_rs_assign; }
#line 3312 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 102: /* assignment_operator: AND_ASSIGN  */
#line 806 "../src/compiler/glsl/glsl_parser.yy"
                      { (yyval.n) = ast_and_assign; }
#line 3318 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 103: /* assignment_operator: XOR_ASSIGN  */
#line 807 "../src/compiler/glsl/glsl_parser.yy"
                      { (yyval.n) = ast_xor_assign; }
#line 3324 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 104: /* assignment_operator: OR_ASSIGN  */
#line 808 "../src/compiler/glsl/glsl_parser.yy"
                      { (yyval.n) = ast_or_assign; }
#line 3330 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 105: /* expression: assignment_expression  */
#line 813 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.expression) = (yyvsp[0].expression);
   }
#line 3338 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 106: /* expression: expression ',' assignment_expression  */
#line 817 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      if ((yyvsp[-2].expression)->oper != ast_sequence) {
         (yyval.expression) = new(ctx) ast_expression(ast_sequence, NULL, NULL, NULL);
         (yyval.expression)->set_location_range((yylsp[-2]), (yylsp[0]));
         (yyval.expression)->expressions.push_tail(& (yyvsp[-2].expression)->link);
      } else {
         (yyval.expression) = (yyvsp[-2].expression);
      }

      (yyval.expression)->expressions.push_tail(& (yyvsp[0].expression)->link);
   }
#line 3355 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 108: /* declaration: function_prototype ';'  */
#line 837 "../src/compiler/glsl/glsl_parser.yy"
   {
      state->symbols->pop_scope();
      (yyval.node) = (yyvsp[-1].function);
   }
#line 3364 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 109: /* declaration: init_declarator_list ';'  */
#line 842 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.node) = (yyvsp[-1].declarator_list);
   }
#line 3372 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 110: /* declaration: PRECISION precision_qualifier type_specifier ';'  */
#line 846 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyvsp[-1].type_specifier)->default_precision = (yyvsp[-2].n);
      (yyval.node) = (yyvsp[-1].type_specifier);
   }
#line 3381 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 111: /* declaration: interface_block  */
#line 851 "../src/compiler/glsl/glsl_parser.yy"
   {
      ast_interface_block *block = (ast_interface_block *) (yyvsp[0].node);
      if (block->layout.has_layout() || block->layout.has_memory()) {
         if (!block->default_layout.merge_qualifier(& (yylsp[0]), state, block->layout, false)) {
            YYERROR;
         }
      }
      block->layout = block->default_layout;
      if (!block->layout.push_to_global(& (yylsp[0]), state)) {
         YYERROR;
      }
      (yyval.node) = (yyvsp[0].node);
   }
#line 3399 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 115: /* function_header_with_parameters: function_header parameter_declaration  */
#line 877 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.function) = (yyvsp[-1].function);
      (yyval.function)->parameters.push_tail(& (yyvsp[0].parameter_declarator)->link);
   }
#line 3408 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 116: /* function_header_with_parameters: function_header_with_parameters ',' parameter_declaration  */
#line 882 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.function) = (yyvsp[-2].function);
      (yyval.function)->parameters.push_tail(& (yyvsp[0].parameter_declarator)->link);
   }
#line 3417 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 117: /* function_header: fully_specified_type variable_identifier '('  */
#line 890 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.function) = new(ctx) ast_function();
      (yyval.function)->set_location((yylsp[-1]));
      (yyval.function)->return_type = (yyvsp[-2].fully_specified_type);
      (yyval.function)->identifier = (yyvsp[-1].identifier);

      if ((yyvsp[-2].fully_specified_type)->qualifier.is_subroutine_decl()) {
         /* add type for IDENTIFIER search */
         state->symbols->add_type((yyvsp[-1].identifier), glsl_type::get_subroutine_instance((yyvsp[-1].identifier)));
      } else
         state->symbols->add_function(new(state) ir_function((yyvsp[-1].identifier)));
      state->symbols->push_scope();
   }
#line 3436 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 118: /* parameter_declarator: type_specifier any_identifier  */
#line 908 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.parameter_declarator) = new(ctx) ast_parameter_declarator();
      (yyval.parameter_declarator)->set_location_range((yylsp[-1]), (yylsp[0]));
      (yyval.parameter_declarator)->type = new(ctx) ast_fully_specified_type();
      (yyval.parameter_declarator)->type->set_location((yylsp[-1]));
      (yyval.parameter_declarator)->type->specifier = (yyvsp[-1].type_specifier);
      (yyval.parameter_declarator)->identifier = (yyvsp[0].identifier);
      state->symbols->add_variable(new(state) ir_variable(NULL, (yyvsp[0].identifier), ir_var_auto));
   }
#line 3451 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 119: /* parameter_declarator: layout_qualifier type_specifier any_identifier  */
#line 919 "../src/compiler/glsl/glsl_parser.yy"
   {
      _mesa_glsl_error(&(yylsp[-2]), state, "is is not allowed on function parameter");
      YYERROR;
   }
#line 3460 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 120: /* parameter_declarator: type_specifier any_identifier array_specifier  */
#line 924 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.parameter_declarator) = new(ctx) ast_parameter_declarator();
      (yyval.parameter_declarator)->set_location_range((yylsp[-2]), (yylsp[0]));
      (yyval.parameter_declarator)->type = new(ctx) ast_fully_specified_type();
      (yyval.parameter_declarator)->type->set_location((yylsp[-2]));
      (yyval.parameter_declarator)->type->specifier = (yyvsp[-2].type_specifier);
      (yyval.parameter_declarator)->identifier = (yyvsp[-1].identifier);
      (yyval.parameter_declarator)->array_specifier = (yyvsp[0].array_specifier);
      state->symbols->add_variable(new(state) ir_variable(NULL, (yyvsp[-1].identifier), ir_var_auto));
   }
#line 3476 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 121: /* parameter_declaration: parameter_qualifier parameter_declarator  */
#line 939 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.parameter_declarator) = (yyvsp[0].parameter_declarator);
      (yyval.parameter_declarator)->type->qualifier = (yyvsp[-1].type_qualifier);
      if (!(yyval.parameter_declarator)->type->qualifier.push_to_global(& (yylsp[-1]), state)) {
         YYERROR;
      }
   }
#line 3488 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 122: /* parameter_declaration: parameter_qualifier parameter_type_specifier  */
#line 947 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.parameter_declarator) = new(ctx) ast_parameter_declarator();
      (yyval.parameter_declarator)->set_location((yylsp[0]));
      (yyval.parameter_declarator)->type = new(ctx) ast_fully_specified_type();
      (yyval.parameter_declarator)->type->set_location_range((yylsp[-1]), (yylsp[0]));
      (yyval.parameter_declarator)->type->qualifier = (yyvsp[-1].type_qualifier);
      if (!(yyval.parameter_declarator)->type->qualifier.push_to_global(& (yylsp[-1]), state)) {
         YYERROR;
      }
      (yyval.parameter_declarator)->type->specifier = (yyvsp[0].type_specifier);
   }
#line 3505 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 123: /* parameter_qualifier: %empty  */
#line 963 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
   }
#line 3513 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 124: /* parameter_qualifier: CONST_TOK parameter_qualifier  */
#line 967 "../src/compiler/glsl/glsl_parser.yy"
   {
      if ((yyvsp[0].type_qualifier).flags.q.constant)
         _mesa_glsl_error(&(yylsp[-1]), state, "duplicate const qualifier");

      (yyval.type_qualifier) = (yyvsp[0].type_qualifier);
      (yyval.type_qualifier).flags.q.constant = 1;
   }
#line 3525 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 125: /* parameter_qualifier: PRECISE parameter_qualifier  */
#line 975 "../src/compiler/glsl/glsl_parser.yy"
   {
      if ((yyvsp[0].type_qualifier).flags.q.precise)
         _mesa_glsl_error(&(yylsp[-1]), state, "duplicate precise qualifier");

      (yyval.type_qualifier) = (yyvsp[0].type_qualifier);
      (yyval.type_qualifier).flags.q.precise = 1;
   }
#line 3537 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 126: /* parameter_qualifier: parameter_direction_qualifier parameter_qualifier  */
#line 983 "../src/compiler/glsl/glsl_parser.yy"
   {
      if (((yyvsp[-1].type_qualifier).flags.q.in || (yyvsp[-1].type_qualifier).flags.q.out) && ((yyvsp[0].type_qualifier).flags.q.in || (yyvsp[0].type_qualifier).flags.q.out))
         _mesa_glsl_error(&(yylsp[-1]), state, "duplicate in/out/inout qualifier");

      if (!state->has_420pack_or_es31() && (yyvsp[0].type_qualifier).flags.q.constant)
         _mesa_glsl_error(&(yylsp[-1]), state, "in/out/inout must come after const "
                                      "or precise");

      (yyval.type_qualifier) = (yyvsp[-1].type_qualifier);
      (yyval.type_qualifier).merge_qualifier(&(yylsp[-1]), state, (yyvsp[0].type_qualifier), false);
   }
#line 3553 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 127: /* parameter_qualifier: precision_qualifier parameter_qualifier  */
#line 995 "../src/compiler/glsl/glsl_parser.yy"
   {
      if ((yyvsp[0].type_qualifier).precision != ast_precision_none)
         _mesa_glsl_error(&(yylsp[-1]), state, "duplicate precision qualifier");

      if (!state->has_420pack_or_es31() &&
          (yyvsp[0].type_qualifier).flags.i != 0)
         _mesa_glsl_error(&(yylsp[-1]), state, "precision qualifiers must come last");

      (yyval.type_qualifier) = (yyvsp[0].type_qualifier);
      (yyval.type_qualifier).precision = (yyvsp[-1].n);
   }
#line 3569 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 128: /* parameter_qualifier: memory_qualifier parameter_qualifier  */
#line 1007 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.type_qualifier) = (yyvsp[-1].type_qualifier);
      (yyval.type_qualifier).merge_qualifier(&(yylsp[-1]), state, (yyvsp[0].type_qualifier), false);
   }
#line 3578 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 129: /* parameter_direction_qualifier: IN_TOK  */
#line 1014 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.in = 1;
   }
#line 3587 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 130: /* parameter_direction_qualifier: OUT_TOK  */
#line 1019 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.out = 1;
   }
#line 3596 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 131: /* parameter_direction_qualifier: INOUT_TOK  */
#line 1024 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.in = 1;
      (yyval.type_qualifier).flags.q.out = 1;
   }
#line 3606 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 134: /* init_declarator_list: init_declarator_list ',' any_identifier  */
#line 1038 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      ast_declaration *decl = new(ctx) ast_declaration((yyvsp[0].identifier), NULL, NULL);
      decl->set_location((yylsp[0]));

      (yyval.declarator_list) = (yyvsp[-2].declarator_list);
      (yyval.declarator_list)->declarations.push_tail(&decl->link);
      state->symbols->add_variable(new(state) ir_variable(NULL, (yyvsp[0].identifier), ir_var_auto));
   }
#line 3620 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 135: /* init_declarator_list: init_declarator_list ',' any_identifier array_specifier  */
#line 1048 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      ast_declaration *decl = new(ctx) ast_declaration((yyvsp[-1].identifier), (yyvsp[0].array_specifier), NULL);
      decl->set_location_range((yylsp[-1]), (yylsp[0]));

      (yyval.declarator_list) = (yyvsp[-3].declarator_list);
      (yyval.declarator_list)->declarations.push_tail(&decl->link);
      state->symbols->add_variable(new(state) ir_variable(NULL, (yyvsp[-1].identifier), ir_var_auto));
   }
#line 3634 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 136: /* init_declarator_list: init_declarator_list ',' any_identifier array_specifier '=' initializer  */
#line 1058 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      ast_declaration *decl = new(ctx) ast_declaration((yyvsp[-3].identifier), (yyvsp[-2].array_specifier), (yyvsp[0].expression));
      decl->set_location_range((yylsp[-3]), (yylsp[-2]));

      (yyval.declarator_list) = (yyvsp[-5].declarator_list);
      (yyval.declarator_list)->declarations.push_tail(&decl->link);
      state->symbols->add_variable(new(state) ir_variable(NULL, (yyvsp[-3].identifier), ir_var_auto));
   }
#line 3648 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 137: /* init_declarator_list: init_declarator_list ',' any_identifier '=' initializer  */
#line 1068 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      ast_declaration *decl = new(ctx) ast_declaration((yyvsp[-2].identifier), NULL, (yyvsp[0].expression));
      decl->set_location((yylsp[-2]));

      (yyval.declarator_list) = (yyvsp[-4].declarator_list);
      (yyval.declarator_list)->declarations.push_tail(&decl->link);
      state->symbols->add_variable(new(state) ir_variable(NULL, (yyvsp[-2].identifier), ir_var_auto));
   }
#line 3662 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 138: /* single_declaration: fully_specified_type  */
#line 1082 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      /* Empty declaration list is valid. */
      (yyval.declarator_list) = new(ctx) ast_declarator_list((yyvsp[0].fully_specified_type));
      (yyval.declarator_list)->set_location((yylsp[0]));
   }
#line 3673 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 139: /* single_declaration: fully_specified_type any_identifier  */
#line 1089 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      ast_declaration *decl = new(ctx) ast_declaration((yyvsp[0].identifier), NULL, NULL);
      decl->set_location((yylsp[0]));

      (yyval.declarator_list) = new(ctx) ast_declarator_list((yyvsp[-1].fully_specified_type));
      (yyval.declarator_list)->set_location_range((yylsp[-1]), (yylsp[0]));
      (yyval.declarator_list)->declarations.push_tail(&decl->link);
      state->symbols->add_variable(new(state) ir_variable(NULL, (yyvsp[0].identifier), ir_var_auto));
   }
#line 3688 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 140: /* single_declaration: fully_specified_type any_identifier array_specifier  */
#line 1100 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      ast_declaration *decl = new(ctx) ast_declaration((yyvsp[-1].identifier), (yyvsp[0].array_specifier), NULL);
      decl->set_location_range((yylsp[-1]), (yylsp[0]));

      (yyval.declarator_list) = new(ctx) ast_declarator_list((yyvsp[-2].fully_specified_type));
      (yyval.declarator_list)->set_location_range((yylsp[-2]), (yylsp[0]));
      (yyval.declarator_list)->declarations.push_tail(&decl->link);
      state->symbols->add_variable(new(state) ir_variable(NULL, (yyvsp[-1].identifier), ir_var_auto));
   }
#line 3703 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 141: /* single_declaration: fully_specified_type any_identifier array_specifier '=' initializer  */
#line 1111 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      ast_declaration *decl = new(ctx) ast_declaration((yyvsp[-3].identifier), (yyvsp[-2].array_specifier), (yyvsp[0].expression));
      decl->set_location_range((yylsp[-3]), (yylsp[-2]));

      (yyval.declarator_list) = new(ctx) ast_declarator_list((yyvsp[-4].fully_specified_type));
      (yyval.declarator_list)->set_location_range((yylsp[-4]), (yylsp[-2]));
      (yyval.declarator_list)->declarations.push_tail(&decl->link);
      state->symbols->add_variable(new(state) ir_variable(NULL, (yyvsp[-3].identifier), ir_var_auto));
   }
#line 3718 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 142: /* single_declaration: fully_specified_type any_identifier '=' initializer  */
#line 1122 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      ast_declaration *decl = new(ctx) ast_declaration((yyvsp[-2].identifier), NULL, (yyvsp[0].expression));
      decl->set_location((yylsp[-2]));

      (yyval.declarator_list) = new(ctx) ast_declarator_list((yyvsp[-3].fully_specified_type));
      (yyval.declarator_list)->set_location_range((yylsp[-3]), (yylsp[-2]));
      (yyval.declarator_list)->declarations.push_tail(&decl->link);
      state->symbols->add_variable(new(state) ir_variable(NULL, (yyvsp[-2].identifier), ir_var_auto));
   }
#line 3733 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 143: /* single_declaration: INVARIANT variable_identifier  */
#line 1133 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      ast_declaration *decl = new(ctx) ast_declaration((yyvsp[0].identifier), NULL, NULL);
      decl->set_location((yylsp[0]));

      (yyval.declarator_list) = new(ctx) ast_declarator_list(NULL);
      (yyval.declarator_list)->set_location_range((yylsp[-1]), (yylsp[0]));
      (yyval.declarator_list)->invariant = true;

      (yyval.declarator_list)->declarations.push_tail(&decl->link);
   }
#line 3749 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 144: /* single_declaration: PRECISE variable_identifier  */
#line 1145 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      ast_declaration *decl = new(ctx) ast_declaration((yyvsp[0].identifier), NULL, NULL);
      decl->set_location((yylsp[0]));

      (yyval.declarator_list) = new(ctx) ast_declarator_list(NULL);
      (yyval.declarator_list)->set_location_range((yylsp[-1]), (yylsp[0]));
      (yyval.declarator_list)->precise = true;

      (yyval.declarator_list)->declarations.push_tail(&decl->link);
   }
#line 3765 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 145: /* fully_specified_type: type_specifier  */
#line 1160 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.fully_specified_type) = new(ctx) ast_fully_specified_type();
      (yyval.fully_specified_type)->set_location((yylsp[0]));
      (yyval.fully_specified_type)->specifier = (yyvsp[0].type_specifier);
   }
#line 3776 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 146: /* fully_specified_type: type_qualifier type_specifier  */
#line 1167 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.fully_specified_type) = new(ctx) ast_fully_specified_type();
      (yyval.fully_specified_type)->set_location_range((yylsp[-1]), (yylsp[0]));
      (yyval.fully_specified_type)->qualifier = (yyvsp[-1].type_qualifier);
      if (!(yyval.fully_specified_type)->qualifier.push_to_global(& (yylsp[-1]), state)) {
         YYERROR;
      }
      (yyval.fully_specified_type)->specifier = (yyvsp[0].type_specifier);
      if ((yyval.fully_specified_type)->specifier->structure != NULL &&
          (yyval.fully_specified_type)->specifier->structure->is_declaration) {
            (yyval.fully_specified_type)->specifier->structure->layout = &(yyval.fully_specified_type)->qualifier;
      }
   }
#line 3795 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 147: /* layout_qualifier: LAYOUT_TOK '(' layout_qualifier_id_list ')'  */
#line 1185 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.type_qualifier) = (yyvsp[-1].type_qualifier);
   }
#line 3803 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 149: /* layout_qualifier_id_list: layout_qualifier_id_list ',' layout_qualifier_id  */
#line 1193 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.type_qualifier) = (yyvsp[-2].type_qualifier);
      if (!(yyval.type_qualifier).merge_qualifier(& (yylsp[0]), state, (yyvsp[0].type_qualifier), true)) {
         YYERROR;
      }
   }
#line 3814 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 150: /* layout_qualifier_id: any_identifier  */
#line 1203 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));

      /* Layout qualifiers for ARB_fragment_coord_conventions. */
      if (!(yyval.type_qualifier).flags.i && (state->ARB_fragment_coord_conventions_enable ||
                          state->is_version(150, 0))) {
         if (match_layout_qualifier((yyvsp[0].identifier), "origin_upper_left", state) == 0) {
            (yyval.type_qualifier).flags.q.origin_upper_left = 1;
         } else if (match_layout_qualifier((yyvsp[0].identifier), "pixel_center_integer",
                                           state) == 0) {
            (yyval.type_qualifier).flags.q.pixel_center_integer = 1;
         }

         if ((yyval.type_qualifier).flags.i && state->ARB_fragment_coord_conventions_warn) {
            _mesa_glsl_warning(& (yylsp[0]), state,
                               "GL_ARB_fragment_coord_conventions layout "
                               "identifier `%s' used", (yyvsp[0].identifier));
         }
      }

      /* Layout qualifiers for AMD/ARB_conservative_depth. */
      if (!(yyval.type_qualifier).flags.i &&
          (state->AMD_conservative_depth_enable ||
           state->ARB_conservative_depth_enable ||
           state->is_version(420, 0))) {
         if (match_layout_qualifier((yyvsp[0].identifier), "depth_any", state) == 0) {
            (yyval.type_qualifier).flags.q.depth_type = 1;
            (yyval.type_qualifier).depth_type = ast_depth_any;
         } else if (match_layout_qualifier((yyvsp[0].identifier), "depth_greater", state) == 0) {
            (yyval.type_qualifier).flags.q.depth_type = 1;
            (yyval.type_qualifier).depth_type = ast_depth_greater;
         } else if (match_layout_qualifier((yyvsp[0].identifier), "depth_less", state) == 0) {
            (yyval.type_qualifier).flags.q.depth_type = 1;
            (yyval.type_qualifier).depth_type = ast_depth_less;
         } else if (match_layout_qualifier((yyvsp[0].identifier), "depth_unchanged",
                                           state) == 0) {
            (yyval.type_qualifier).flags.q.depth_type = 1;
            (yyval.type_qualifier).depth_type = ast_depth_unchanged;
         }

         if ((yyval.type_qualifier).flags.i && state->AMD_conservative_depth_warn) {
            _mesa_glsl_warning(& (yylsp[0]), state,
                               "GL_AMD_conservative_depth "
                               "layout qualifier `%s' is used", (yyvsp[0].identifier));
         }
         if ((yyval.type_qualifier).flags.i && state->ARB_conservative_depth_warn) {
            _mesa_glsl_warning(& (yylsp[0]), state,
                               "GL_ARB_conservative_depth "
                               "layout qualifier `%s' is used", (yyvsp[0].identifier));
         }
      }

      /* See also interface_block_layout_qualifier. */
      if (!(yyval.type_qualifier).flags.i && state->has_uniform_buffer_objects()) {
         if (match_layout_qualifier((yyvsp[0].identifier), "std140", state) == 0) {
            (yyval.type_qualifier).flags.q.std140 = 1;
         } else if (match_layout_qualifier((yyvsp[0].identifier), "shared", state) == 0) {
            (yyval.type_qualifier).flags.q.shared = 1;
         } else if (match_layout_qualifier((yyvsp[0].identifier), "std430", state) == 0) {
            (yyval.type_qualifier).flags.q.std430 = 1;
         } else if (match_layout_qualifier((yyvsp[0].identifier), "column_major", state) == 0) {
            (yyval.type_qualifier).flags.q.column_major = 1;
         /* "row_major" is a reserved word in GLSL 1.30+. Its token is parsed
          * below in the interface_block_layout_qualifier rule.
          *
          * It is not a reserved word in GLSL ES 3.00, so it's handled here as
          * an identifier.
          *
          * Also, this takes care of alternate capitalizations of
          * "row_major" (which is necessary because layout qualifiers
          * are case-insensitive in desktop GLSL).
          */
         } else if (match_layout_qualifier((yyvsp[0].identifier), "row_major", state) == 0) {
            (yyval.type_qualifier).flags.q.row_major = 1;
         /* "packed" is a reserved word in GLSL, and its token is
          * parsed below in the interface_block_layout_qualifier rule.
          * However, we must take care of alternate capitalizations of
          * "packed", because layout qualifiers are case-insensitive
          * in desktop GLSL.
          */
         } else if (match_layout_qualifier((yyvsp[0].identifier), "packed", state) == 0) {
           (yyval.type_qualifier).flags.q.packed = 1;
         }

         if ((yyval.type_qualifier).flags.i && state->ARB_uniform_buffer_object_warn) {
            _mesa_glsl_warning(& (yylsp[0]), state,
                               "#version 140 / GL_ARB_uniform_buffer_object "
                               "layout qualifier `%s' is used", (yyvsp[0].identifier));
         }
      }

      /* Layout qualifiers for GLSL 1.50 geometry shaders. */
      if (!(yyval.type_qualifier).flags.i) {
         static const struct {
            const char *s;
            GLenum e;
         } map[] = {
                 { "points", GL_POINTS },
                 { "lines", GL_LINES },
                 { "lines_adjacency", GL_LINES_ADJACENCY },
                 { "line_strip", GL_LINE_STRIP },
                 { "triangles", GL_TRIANGLES },
                 { "triangles_adjacency", GL_TRIANGLES_ADJACENCY },
                 { "triangle_strip", GL_TRIANGLE_STRIP },
         };
         for (unsigned i = 0; i < ARRAY_SIZE(map); i++) {
            if (match_layout_qualifier((yyvsp[0].identifier), map[i].s, state) == 0) {
               (yyval.type_qualifier).flags.q.prim_type = 1;
               (yyval.type_qualifier).prim_type = map[i].e;
               break;
            }
         }

         if ((yyval.type_qualifier).flags.i && !state->has_geometry_shader() &&
             !state->has_tessellation_shader()) {
            _mesa_glsl_error(& (yylsp[0]), state, "#version 150 layout "
                             "qualifier `%s' used", (yyvsp[0].identifier));
         }
      }

      /* Layout qualifiers for ARB_shader_image_load_store. */
      if (state->has_shader_image_load_store()) {
         if (!(yyval.type_qualifier).flags.i) {
            static const struct {
               const char *name;
               enum pipe_format format;
               glsl_base_type base_type;
               /** Minimum desktop GLSL version required for the image
                * format.  Use 130 if already present in the original
                * ARB extension.
                */
               unsigned required_glsl;
               /** Minimum GLSL ES version required for the image format. */
               unsigned required_essl;
               /* NV_image_formats */
               bool nv_image_formats;
               bool ext_qualifiers;
            } map[] = {
               { "rgba32f", PIPE_FORMAT_R32G32B32A32_FLOAT, GLSL_TYPE_FLOAT, 130, 310, false, false },
               { "rgba16f", PIPE_FORMAT_R16G16B16A16_FLOAT, GLSL_TYPE_FLOAT, 130, 310, false, false },
               { "rg32f", PIPE_FORMAT_R32G32_FLOAT, GLSL_TYPE_FLOAT, 130, 0, true, false },
               { "rg16f", PIPE_FORMAT_R16G16_FLOAT, GLSL_TYPE_FLOAT, 130, 0, true, false },
               { "r11f_g11f_b10f", PIPE_FORMAT_R11G11B10_FLOAT, GLSL_TYPE_FLOAT, 130, 0, true, false },
               { "r32f", PIPE_FORMAT_R32_FLOAT, GLSL_TYPE_FLOAT, 130, 310, false, false },
               { "r16f", PIPE_FORMAT_R16_FLOAT, GLSL_TYPE_FLOAT, 130, 0, true, false },
               { "rgba32ui", PIPE_FORMAT_R32G32B32A32_UINT, GLSL_TYPE_UINT, 130, 310, false, false },
               { "rgba16ui", PIPE_FORMAT_R16G16B16A16_UINT, GLSL_TYPE_UINT, 130, 310, false, false },
               { "rgb10_a2ui", PIPE_FORMAT_R10G10B10A2_UINT, GLSL_TYPE_UINT, 130, 0, true, false },
               { "rgba8ui", PIPE_FORMAT_R8G8B8A8_UINT, GLSL_TYPE_UINT, 130, 310, false, false },
               { "rg32ui", PIPE_FORMAT_R32G32_UINT, GLSL_TYPE_UINT, 130, 0, true, false },
               { "rg16ui", PIPE_FORMAT_R16G16_UINT, GLSL_TYPE_UINT, 130, 0, true, false },
               { "rg8ui", PIPE_FORMAT_R8G8_UINT, GLSL_TYPE_UINT, 130, 0, true, false },
               { "r32ui", PIPE_FORMAT_R32_UINT, GLSL_TYPE_UINT, 130, 310, false, false },
               { "r16ui", PIPE_FORMAT_R16_UINT, GLSL_TYPE_UINT, 130, 0, true, false },
               { "r8ui", PIPE_FORMAT_R8_UINT, GLSL_TYPE_UINT, 130, 0, true, false },
               { "rgba32i", PIPE_FORMAT_R32G32B32A32_SINT, GLSL_TYPE_INT, 130, 310, false, false },
               { "rgba16i", PIPE_FORMAT_R16G16B16A16_SINT, GLSL_TYPE_INT, 130, 310, false, false },
               { "rgba8i", PIPE_FORMAT_R8G8B8A8_SINT, GLSL_TYPE_INT, 130, 310, false, false },
               { "rg32i", PIPE_FORMAT_R32G32_SINT, GLSL_TYPE_INT, 130, 0, true, false },
               { "rg16i", PIPE_FORMAT_R16G16_SINT, GLSL_TYPE_INT, 130, 0, true, false },
               { "rg8i", PIPE_FORMAT_R8G8_SINT, GLSL_TYPE_INT, 130, 0, true, false },
               { "r32i", PIPE_FORMAT_R32_SINT, GLSL_TYPE_INT, 130, 310, false, false },
               { "r16i", PIPE_FORMAT_R16_SINT, GLSL_TYPE_INT, 130, 0, true, false },
               { "r8i", PIPE_FORMAT_R8_SINT, GLSL_TYPE_INT, 130, 0, true, false },
               { "rgba16", PIPE_FORMAT_R16G16B16A16_UNORM, GLSL_TYPE_FLOAT, 130, 0, true, false },
               { "rgb10_a2", PIPE_FORMAT_R10G10B10A2_UNORM, GLSL_TYPE_FLOAT, 130, 0, true, false },
               { "rgba8", PIPE_FORMAT_R8G8B8A8_UNORM, GLSL_TYPE_FLOAT, 130, 310, false, false },
               { "rg16", PIPE_FORMAT_R16G16_UNORM, GLSL_TYPE_FLOAT, 130, 0, true, false },
               { "rg8", PIPE_FORMAT_R8G8_UNORM, GLSL_TYPE_FLOAT, 130, 0, true, false },
               { "r16", PIPE_FORMAT_R16_UNORM, GLSL_TYPE_FLOAT, 130, 0, true, false },
               { "r8", PIPE_FORMAT_R8_UNORM, GLSL_TYPE_FLOAT, 130, 0, true, false },
               { "rgba16_snorm", PIPE_FORMAT_R16G16B16A16_SNORM, GLSL_TYPE_FLOAT, 130, 0, true, false },
               { "rgba8_snorm", PIPE_FORMAT_R8G8B8A8_SNORM, GLSL_TYPE_FLOAT, 130, 310, false, false },
               { "rg16_snorm", PIPE_FORMAT_R16G16_SNORM, GLSL_TYPE_FLOAT, 130, 0, true, false },
               { "rg8_snorm", PIPE_FORMAT_R8G8_SNORM, GLSL_TYPE_FLOAT, 130, 0, true, false },
               { "r16_snorm", PIPE_FORMAT_R16_SNORM, GLSL_TYPE_FLOAT, 130, 0, true, false },
               { "r8_snorm", PIPE_FORMAT_R8_SNORM, GLSL_TYPE_FLOAT, 130, 0, true, false },

               /* From GL_EXT_shader_image_load_store: */
               /* base_type is incorrect but it'll be patched later when we know
                * the variable type. See ast_to_hir.cpp */
               { "size1x8", PIPE_FORMAT_R8_SINT, GLSL_TYPE_VOID, 130, 0, false, true },
               { "size1x16", PIPE_FORMAT_R16_SINT, GLSL_TYPE_VOID, 130, 0, false, true },
               { "size1x32", PIPE_FORMAT_R32_SINT, GLSL_TYPE_VOID, 130, 0, false, true },
               { "size2x32", PIPE_FORMAT_R32G32_SINT, GLSL_TYPE_VOID, 130, 0, false, true },
               { "size4x32", PIPE_FORMAT_R32G32B32A32_SINT, GLSL_TYPE_VOID, 130, 0, false, true },
            };

            for (unsigned i = 0; i < ARRAY_SIZE(map); i++) {
               if ((state->is_version(map[i].required_glsl,
                                      map[i].required_essl) ||
                    (state->NV_image_formats_enable &&
                     map[i].nv_image_formats)) &&
                   match_layout_qualifier((yyvsp[0].identifier), map[i].name, state) == 0) {
                  /* Skip ARB_shader_image_load_store qualifiers if not enabled */
                  if (!map[i].ext_qualifiers && !(state->ARB_shader_image_load_store_enable ||
                                                  state->is_version(420, 310))) {
                     continue;
                  }
                  /* Skip EXT_shader_image_load_store qualifiers if not enabled */
                  if (map[i].ext_qualifiers && !state->EXT_shader_image_load_store_enable) {
                     continue;
                  }
                  (yyval.type_qualifier).flags.q.explicit_image_format = 1;
                  (yyval.type_qualifier).image_format = map[i].format;
                  (yyval.type_qualifier).image_base_type = map[i].base_type;
                  break;
               }
            }
         }
      }

      if (!(yyval.type_qualifier).flags.i) {
         if (match_layout_qualifier((yyvsp[0].identifier), "early_fragment_tests", state) == 0) {
            /* From section 4.4.1.3 of the GLSL 4.50 specification
             * (Fragment Shader Inputs):
             *
             *  "Fragment shaders also allow the following layout
             *   qualifier on in only (not with variable declarations)
             *     layout-qualifier-id
             *        early_fragment_tests
             *   [...]"
             */
            if (state->stage != MESA_SHADER_FRAGMENT) {
               _mesa_glsl_error(& (yylsp[0]), state,
                                "early_fragment_tests layout qualifier only "
                                "valid in fragment shaders");
            }

            (yyval.type_qualifier).flags.q.early_fragment_tests = 1;
         }

         if (match_layout_qualifier((yyvsp[0].identifier), "inner_coverage", state) == 0) {
            if (state->stage != MESA_SHADER_FRAGMENT) {
               _mesa_glsl_error(& (yylsp[0]), state,
                                "inner_coverage layout qualifier only "
                                "valid in fragment shaders");
            }

            if (state->INTEL_conservative_rasterization_enable) {
               (yyval.type_qualifier).flags.q.inner_coverage = 1;
            } else {
               _mesa_glsl_error(& (yylsp[0]), state,
                                "inner_coverage layout qualifier present, "
                                "but the INTEL_conservative_rasterization extension "
                                "is not enabled.");
            }
         }

         if (match_layout_qualifier((yyvsp[0].identifier), "post_depth_coverage", state) == 0) {
            if (state->stage != MESA_SHADER_FRAGMENT) {
               _mesa_glsl_error(& (yylsp[0]), state,
                                "post_depth_coverage layout qualifier only "
                                "valid in fragment shaders");
            }

            if (state->ARB_post_depth_coverage_enable ||
                state->INTEL_conservative_rasterization_enable) {
               (yyval.type_qualifier).flags.q.post_depth_coverage = 1;
            } else {
               _mesa_glsl_error(& (yylsp[0]), state,
                                "post_depth_coverage layout qualifier present, "
                                "but the GL_ARB_post_depth_coverage extension "
                                "is not enabled.");
            }
         }

         if ((yyval.type_qualifier).flags.q.post_depth_coverage && (yyval.type_qualifier).flags.q.inner_coverage) {
            _mesa_glsl_error(& (yylsp[0]), state,
                             "post_depth_coverage & inner_coverage layout qualifiers "
                             "are mutually exclusive");
         }
      }

      const bool pixel_interlock_ordered = match_layout_qualifier((yyvsp[0].identifier),
         "pixel_interlock_ordered", state) == 0;
      const bool pixel_interlock_unordered = match_layout_qualifier((yyvsp[0].identifier),
         "pixel_interlock_unordered", state) == 0;
      const bool sample_interlock_ordered = match_layout_qualifier((yyvsp[0].identifier),
         "sample_interlock_ordered", state) == 0;
      const bool sample_interlock_unordered = match_layout_qualifier((yyvsp[0].identifier),
         "sample_interlock_unordered", state) == 0;

      if (pixel_interlock_ordered + pixel_interlock_unordered +
          sample_interlock_ordered + sample_interlock_unordered > 0 &&
          state->stage != MESA_SHADER_FRAGMENT) {
         _mesa_glsl_error(& (yylsp[0]), state, "interlock layout qualifiers: "
                          "pixel_interlock_ordered, pixel_interlock_unordered, "
                          "sample_interlock_ordered and sample_interlock_unordered, "
                          "only valid in fragment shader input layout declaration.");
      } else if (pixel_interlock_ordered + pixel_interlock_unordered +
                 sample_interlock_ordered + sample_interlock_unordered > 0 &&
                 !state->ARB_fragment_shader_interlock_enable &&
                 !state->NV_fragment_shader_interlock_enable) {
         _mesa_glsl_error(& (yylsp[0]), state,
                          "interlock layout qualifier present, but the "
                          "GL_ARB_fragment_shader_interlock or "
                          "GL_NV_fragment_shader_interlock extension is not "
                          "enabled.");
      } else {
         (yyval.type_qualifier).flags.q.pixel_interlock_ordered = pixel_interlock_ordered;
         (yyval.type_qualifier).flags.q.pixel_interlock_unordered = pixel_interlock_unordered;
         (yyval.type_qualifier).flags.q.sample_interlock_ordered = sample_interlock_ordered;
         (yyval.type_qualifier).flags.q.sample_interlock_unordered = sample_interlock_unordered;
      }

      /* Layout qualifiers for tessellation evaluation shaders. */
      if (!(yyval.type_qualifier).flags.i) {
         static const struct {
            const char *s;
            GLenum e;
         } map[] = {
                 /* triangles already parsed by gs-specific code */
                 { "quads", GL_QUADS },
                 { "isolines", GL_ISOLINES },
         };
         for (unsigned i = 0; i < ARRAY_SIZE(map); i++) {
            if (match_layout_qualifier((yyvsp[0].identifier), map[i].s, state) == 0) {
               (yyval.type_qualifier).flags.q.prim_type = 1;
               (yyval.type_qualifier).prim_type = map[i].e;
               break;
            }
         }

         if ((yyval.type_qualifier).flags.i && !state->has_tessellation_shader()) {
            _mesa_glsl_error(& (yylsp[0]), state,
                             "primitive mode qualifier `%s' requires "
                             "GLSL 4.00 or ARB_tessellation_shader", (yyvsp[0].identifier));
         }
      }
      if (!(yyval.type_qualifier).flags.i) {
         static const struct {
            const char *s;
            enum gl_tess_spacing e;
         } map[] = {
                 { "equal_spacing", TESS_SPACING_EQUAL },
                 { "fractional_odd_spacing", TESS_SPACING_FRACTIONAL_ODD },
                 { "fractional_even_spacing", TESS_SPACING_FRACTIONAL_EVEN },
         };
         for (unsigned i = 0; i < ARRAY_SIZE(map); i++) {
            if (match_layout_qualifier((yyvsp[0].identifier), map[i].s, state) == 0) {
               (yyval.type_qualifier).flags.q.vertex_spacing = 1;
               (yyval.type_qualifier).vertex_spacing = map[i].e;
               break;
            }
         }

         if ((yyval.type_qualifier).flags.i && !state->has_tessellation_shader()) {
            _mesa_glsl_error(& (yylsp[0]), state,
                             "vertex spacing qualifier `%s' requires "
                             "GLSL 4.00 or ARB_tessellation_shader", (yyvsp[0].identifier));
         }
      }
      if (!(yyval.type_qualifier).flags.i) {
         if (match_layout_qualifier((yyvsp[0].identifier), "cw", state) == 0) {
            (yyval.type_qualifier).flags.q.ordering = 1;
            (yyval.type_qualifier).ordering = GL_CW;
         } else if (match_layout_qualifier((yyvsp[0].identifier), "ccw", state) == 0) {
            (yyval.type_qualifier).flags.q.ordering = 1;
            (yyval.type_qualifier).ordering = GL_CCW;
         }

         if ((yyval.type_qualifier).flags.i && !state->has_tessellation_shader()) {
            _mesa_glsl_error(& (yylsp[0]), state,
                             "ordering qualifier `%s' requires "
                             "GLSL 4.00 or ARB_tessellation_shader", (yyvsp[0].identifier));
         }
      }
      if (!(yyval.type_qualifier).flags.i) {
         if (match_layout_qualifier((yyvsp[0].identifier), "point_mode", state) == 0) {
            (yyval.type_qualifier).flags.q.point_mode = 1;
            (yyval.type_qualifier).point_mode = true;
         }

         if ((yyval.type_qualifier).flags.i && !state->has_tessellation_shader()) {
            _mesa_glsl_error(& (yylsp[0]), state,
                             "qualifier `point_mode' requires "
                             "GLSL 4.00 or ARB_tessellation_shader");
         }
      }

      if (!(yyval.type_qualifier).flags.i) {
         static const struct {
            const char *s;
            uint32_t mask;
         } map[] = {
                 { "blend_support_multiply",       BITFIELD_BIT(BLEND_MULTIPLY) },
                 { "blend_support_screen",         BITFIELD_BIT(BLEND_SCREEN) },
                 { "blend_support_overlay",        BITFIELD_BIT(BLEND_OVERLAY) },
                 { "blend_support_darken",         BITFIELD_BIT(BLEND_DARKEN) },
                 { "blend_support_lighten",        BITFIELD_BIT(BLEND_LIGHTEN) },
                 { "blend_support_colordodge",     BITFIELD_BIT(BLEND_COLORDODGE) },
                 { "blend_support_colorburn",      BITFIELD_BIT(BLEND_COLORBURN) },
                 { "blend_support_hardlight",      BITFIELD_BIT(BLEND_HARDLIGHT) },
                 { "blend_support_softlight",      BITFIELD_BIT(BLEND_SOFTLIGHT) },
                 { "blend_support_difference",     BITFIELD_BIT(BLEND_DIFFERENCE) },
                 { "blend_support_exclusion",      BITFIELD_BIT(BLEND_EXCLUSION) },
                 { "blend_support_hsl_hue",        BITFIELD_BIT(BLEND_HSL_HUE) },
                 { "blend_support_hsl_saturation", BITFIELD_BIT(BLEND_HSL_SATURATION) },
                 { "blend_support_hsl_color",      BITFIELD_BIT(BLEND_HSL_COLOR) },
                 { "blend_support_hsl_luminosity", BITFIELD_BIT(BLEND_HSL_LUMINOSITY) },
                 { "blend_support_all_equations",  (1u << (BLEND_HSL_LUMINOSITY + 1)) - 2 },
         };
         for (unsigned i = 0; i < ARRAY_SIZE(map); i++) {
            if (match_layout_qualifier((yyvsp[0].identifier), map[i].s, state) == 0) {
               (yyval.type_qualifier).flags.q.blend_support = 1;
               state->fs_blend_support |= map[i].mask;
               break;
            }
         }

         if ((yyval.type_qualifier).flags.i &&
             !state->KHR_blend_equation_advanced_enable &&
             !state->is_version(0, 320)) {
            _mesa_glsl_error(& (yylsp[0]), state,
                             "advanced blending layout qualifiers require "
                             "ESSL 3.20 or KHR_blend_equation_advanced");
         }

         if ((yyval.type_qualifier).flags.i && state->stage != MESA_SHADER_FRAGMENT) {
            _mesa_glsl_error(& (yylsp[0]), state,
                             "advanced blending layout qualifiers only "
                             "valid in fragment shaders");
         }
      }

      /* Layout qualifiers for ARB_compute_variable_group_size. */
      if (!(yyval.type_qualifier).flags.i) {
         if (match_layout_qualifier((yyvsp[0].identifier), "local_size_variable", state) == 0) {
            (yyval.type_qualifier).flags.q.local_size_variable = 1;
         }

         if ((yyval.type_qualifier).flags.i && !state->ARB_compute_variable_group_size_enable) {
            _mesa_glsl_error(& (yylsp[0]), state,
                             "qualifier `local_size_variable` requires "
                             "ARB_compute_variable_group_size");
         }
      }

      /* Layout qualifiers for ARB_bindless_texture. */
      if (!(yyval.type_qualifier).flags.i) {
         if (match_layout_qualifier((yyvsp[0].identifier), "bindless_sampler", state) == 0)
            (yyval.type_qualifier).flags.q.bindless_sampler = 1;
         if (match_layout_qualifier((yyvsp[0].identifier), "bound_sampler", state) == 0)
            (yyval.type_qualifier).flags.q.bound_sampler = 1;

         if (state->has_shader_image_load_store()) {
            if (match_layout_qualifier((yyvsp[0].identifier), "bindless_image", state) == 0)
               (yyval.type_qualifier).flags.q.bindless_image = 1;
            if (match_layout_qualifier((yyvsp[0].identifier), "bound_image", state) == 0)
               (yyval.type_qualifier).flags.q.bound_image = 1;
         }

         if ((yyval.type_qualifier).flags.i && !state->has_bindless()) {
            _mesa_glsl_error(& (yylsp[0]), state,
                             "qualifier `%s` requires "
                             "ARB_bindless_texture", (yyvsp[0].identifier));
         }
      }

      if (!(yyval.type_qualifier).flags.i &&
          state->EXT_shader_framebuffer_fetch_non_coherent_enable) {
         if (match_layout_qualifier((yyvsp[0].identifier), "noncoherent", state) == 0)
            (yyval.type_qualifier).flags.q.non_coherent = 1;
      }

      // Layout qualifiers for NV_compute_shader_derivatives.
      if (!(yyval.type_qualifier).flags.i) {
         if (match_layout_qualifier((yyvsp[0].identifier), "derivative_group_quadsNV", state) == 0) {
            (yyval.type_qualifier).flags.q.derivative_group = 1;
            (yyval.type_qualifier).derivative_group = DERIVATIVE_GROUP_QUADS;
         } else if (match_layout_qualifier((yyvsp[0].identifier), "derivative_group_linearNV", state) == 0) {
            (yyval.type_qualifier).flags.q.derivative_group = 1;
            (yyval.type_qualifier).derivative_group = DERIVATIVE_GROUP_LINEAR;
         }

         if ((yyval.type_qualifier).flags.i) {
            if (!state->has_compute_shader()) {
               _mesa_glsl_error(& (yylsp[0]), state,
                                "qualifier `%s' requires "
                                "a compute shader", (yyvsp[0].identifier));
            }

            if (!state->NV_compute_shader_derivatives_enable) {
               _mesa_glsl_error(& (yylsp[0]), state,
                                "qualifier `%s' requires "
                                "NV_compute_shader_derivatives", (yyvsp[0].identifier));
            }

            if (state->NV_compute_shader_derivatives_warn) {
               _mesa_glsl_warning(& (yylsp[0]), state,
                                  "NV_compute_shader_derivatives layout "
                                  "qualifier `%s' used", (yyvsp[0].identifier));
            }
         }
      }

      /* Layout qualifier for NV_viewport_array2. */
      if (!(yyval.type_qualifier).flags.i && state->stage != MESA_SHADER_FRAGMENT) {
         if (match_layout_qualifier((yyvsp[0].identifier), "viewport_relative", state) == 0) {
            (yyval.type_qualifier).flags.q.viewport_relative = 1;
         }

         if ((yyval.type_qualifier).flags.i && !state->NV_viewport_array2_enable) {
            _mesa_glsl_error(& (yylsp[0]), state,
                             "qualifier `%s' requires "
                             "GL_NV_viewport_array2", (yyvsp[0].identifier));
         }

         if ((yyval.type_qualifier).flags.i && state->NV_viewport_array2_warn) {
            _mesa_glsl_warning(& (yylsp[0]), state,
                               "GL_NV_viewport_array2 layout "
                               "identifier `%s' used", (yyvsp[0].identifier));
         }
      }

      if (!(yyval.type_qualifier).flags.i) {
         _mesa_glsl_error(& (yylsp[0]), state, "unrecognized layout identifier "
                          "`%s'", (yyvsp[0].identifier));
         YYERROR;
      }
   }
#line 4341 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 151: /* layout_qualifier_id: any_identifier '=' constant_expression  */
#line 1726 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      void *ctx = state->linalloc;

      if ((yyvsp[0].expression)->oper != ast_int_constant &&
          (yyvsp[0].expression)->oper != ast_uint_constant &&
          !state->has_enhanced_layouts()) {
         _mesa_glsl_error(& (yylsp[-2]), state,
                          "compile-time constant expressions require "
                          "GLSL 4.40 or ARB_enhanced_layouts");
      }

      if (match_layout_qualifier("align", (yyvsp[-2].identifier), state) == 0) {
         if (!state->has_enhanced_layouts()) {
            _mesa_glsl_error(& (yylsp[-2]), state,
                             "align qualifier requires "
                             "GLSL 4.40 or ARB_enhanced_layouts");
         } else {
            (yyval.type_qualifier).flags.q.explicit_align = 1;
            (yyval.type_qualifier).align = (yyvsp[0].expression);
         }
      }

      if (match_layout_qualifier("location", (yyvsp[-2].identifier), state) == 0) {
         (yyval.type_qualifier).flags.q.explicit_location = 1;

         if ((yyval.type_qualifier).flags.q.attribute == 1 &&
             state->ARB_explicit_attrib_location_warn) {
            _mesa_glsl_warning(& (yylsp[-2]), state,
                               "GL_ARB_explicit_attrib_location layout "
                               "identifier `%s' used", (yyvsp[-2].identifier));
         }
         (yyval.type_qualifier).location = (yyvsp[0].expression);
      }

      if (match_layout_qualifier("component", (yyvsp[-2].identifier), state) == 0) {
         if (!state->has_enhanced_layouts()) {
            _mesa_glsl_error(& (yylsp[-2]), state,
                             "component qualifier requires "
                             "GLSL 4.40 or ARB_enhanced_layouts");
         } else {
            (yyval.type_qualifier).flags.q.explicit_component = 1;
            (yyval.type_qualifier).component = (yyvsp[0].expression);
         }
      }

      if (match_layout_qualifier("index", (yyvsp[-2].identifier), state) == 0) {
         if (state->es_shader && !state->EXT_blend_func_extended_enable) {
            _mesa_glsl_error(& (yylsp[0]), state, "index layout qualifier requires EXT_blend_func_extended");
            YYERROR;
         }

         (yyval.type_qualifier).flags.q.explicit_index = 1;
         (yyval.type_qualifier).index = (yyvsp[0].expression);
      }

      if ((state->has_420pack_or_es31() ||
           state->has_atomic_counters() ||
           state->has_shader_storage_buffer_objects()) &&
          match_layout_qualifier("binding", (yyvsp[-2].identifier), state) == 0) {
         (yyval.type_qualifier).flags.q.explicit_binding = 1;
         (yyval.type_qualifier).binding = (yyvsp[0].expression);
      }

      if ((state->has_atomic_counters() ||
           state->has_enhanced_layouts()) &&
          match_layout_qualifier("offset", (yyvsp[-2].identifier), state) == 0) {
         (yyval.type_qualifier).flags.q.explicit_offset = 1;
         (yyval.type_qualifier).offset = (yyvsp[0].expression);
      }

      if (match_layout_qualifier("max_vertices", (yyvsp[-2].identifier), state) == 0) {
         (yyval.type_qualifier).flags.q.max_vertices = 1;
         (yyval.type_qualifier).max_vertices = new(ctx) ast_layout_expression((yylsp[-2]), (yyvsp[0].expression));
         if (!state->has_geometry_shader()) {
            _mesa_glsl_error(& (yylsp[0]), state,
                             "#version 150 max_vertices qualifier "
                             "specified", (yyvsp[0].expression));
         }
      }

      if (state->stage == MESA_SHADER_GEOMETRY) {
         if (match_layout_qualifier("stream", (yyvsp[-2].identifier), state) == 0 &&
             state->check_explicit_attrib_stream_allowed(& (yylsp[0]))) {
            (yyval.type_qualifier).flags.q.stream = 1;
            (yyval.type_qualifier).flags.q.explicit_stream = 1;
            (yyval.type_qualifier).stream = (yyvsp[0].expression);
         }
      }

      if (state->has_enhanced_layouts()) {
         if (match_layout_qualifier("xfb_buffer", (yyvsp[-2].identifier), state) == 0) {
            (yyval.type_qualifier).flags.q.xfb_buffer = 1;
            (yyval.type_qualifier).flags.q.explicit_xfb_buffer = 1;
            (yyval.type_qualifier).xfb_buffer = (yyvsp[0].expression);
         }

         if (match_layout_qualifier("xfb_offset", (yyvsp[-2].identifier), state) == 0) {
            (yyval.type_qualifier).flags.q.explicit_xfb_offset = 1;
            (yyval.type_qualifier).offset = (yyvsp[0].expression);
         }

         if (match_layout_qualifier("xfb_stride", (yyvsp[-2].identifier), state) == 0) {
            (yyval.type_qualifier).flags.q.xfb_stride = 1;
            (yyval.type_qualifier).flags.q.explicit_xfb_stride = 1;
            (yyval.type_qualifier).xfb_stride = (yyvsp[0].expression);
         }
      }

      static const char * const local_size_qualifiers[3] = {
         "local_size_x",
         "local_size_y",
         "local_size_z",
      };
      for (int i = 0; i < 3; i++) {
         if (match_layout_qualifier(local_size_qualifiers[i], (yyvsp[-2].identifier),
                                    state) == 0) {
            if (!state->has_compute_shader()) {
               _mesa_glsl_error(& (yylsp[0]), state,
                                "%s qualifier requires GLSL 4.30 or "
                                "GLSL ES 3.10 or ARB_compute_shader",
                                local_size_qualifiers[i]);
               YYERROR;
            } else {
               (yyval.type_qualifier).flags.q.local_size |= (1 << i);
               (yyval.type_qualifier).local_size[i] = new(ctx) ast_layout_expression((yylsp[-2]), (yyvsp[0].expression));
            }
            break;
         }
      }

      if (match_layout_qualifier("invocations", (yyvsp[-2].identifier), state) == 0) {
         (yyval.type_qualifier).flags.q.invocations = 1;
         (yyval.type_qualifier).invocations = new(ctx) ast_layout_expression((yylsp[-2]), (yyvsp[0].expression));
         if (!state->is_version(400, 320) &&
             !state->ARB_gpu_shader5_enable &&
             !state->OES_geometry_shader_enable &&
             !state->EXT_geometry_shader_enable) {
            _mesa_glsl_error(& (yylsp[0]), state,
                             "GL_ARB_gpu_shader5 invocations "
                             "qualifier specified");
         }
      }

      /* Layout qualifiers for tessellation control shaders. */
      if (match_layout_qualifier("vertices", (yyvsp[-2].identifier), state) == 0) {
         (yyval.type_qualifier).flags.q.vertices = 1;
         (yyval.type_qualifier).vertices = new(ctx) ast_layout_expression((yylsp[-2]), (yyvsp[0].expression));
         if (!state->has_tessellation_shader()) {
            _mesa_glsl_error(& (yylsp[-2]), state,
                             "vertices qualifier requires GLSL 4.00 or "
                             "ARB_tessellation_shader");
         }
      }

      /* If the identifier didn't match any known layout identifiers,
       * emit an error.
       */
      if (!(yyval.type_qualifier).flags.i) {
         _mesa_glsl_error(& (yylsp[-2]), state, "unrecognized layout identifier "
                          "`%s'", (yyvsp[-2].identifier));
         YYERROR;
      }
   }
#line 4510 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 152: /* layout_qualifier_id: interface_block_layout_qualifier  */
#line 1891 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.type_qualifier) = (yyvsp[0].type_qualifier);
      /* Layout qualifiers for ARB_uniform_buffer_object. */
      if ((yyval.type_qualifier).flags.q.uniform && !state->has_uniform_buffer_objects()) {
         _mesa_glsl_error(& (yylsp[0]), state,
                          "#version 140 / GL_ARB_uniform_buffer_object "
                          "layout qualifier `uniform' is used");
      } else if ((yyval.type_qualifier).flags.q.uniform && state->ARB_uniform_buffer_object_warn) {
         _mesa_glsl_warning(& (yylsp[0]), state,
                            "#version 140 / GL_ARB_uniform_buffer_object "
                            "layout qualifier `uniform' is used");
      }
   }
#line 4528 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 153: /* interface_block_layout_qualifier: ROW_MAJOR  */
#line 1917 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.row_major = 1;
   }
#line 4537 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 154: /* interface_block_layout_qualifier: PACKED_TOK  */
#line 1922 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.packed = 1;
   }
#line 4546 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 155: /* interface_block_layout_qualifier: SHARED  */
#line 1927 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.shared = 1;
   }
#line 4555 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 156: /* subroutine_qualifier: SUBROUTINE  */
#line 1935 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.subroutine = 1;
   }
#line 4564 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 157: /* subroutine_qualifier: SUBROUTINE '(' subroutine_type_list ')'  */
#line 1940 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.subroutine = 1;
      (yyval.type_qualifier).subroutine_list = (yyvsp[-1].subroutine_list);
   }
#line 4574 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 158: /* subroutine_type_list: any_identifier  */
#line 1949 "../src/compiler/glsl/glsl_parser.yy"
   {
        void *ctx = state->linalloc;
        ast_declaration *decl = new(ctx)  ast_declaration((yyvsp[0].identifier), NULL, NULL);
        decl->set_location((yylsp[0]));

        (yyval.subroutine_list) = new(ctx) ast_subroutine_list();
        (yyval.subroutine_list)->declarations.push_tail(&decl->link);
   }
#line 4587 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 159: /* subroutine_type_list: subroutine_type_list ',' any_identifier  */
#line 1958 "../src/compiler/glsl/glsl_parser.yy"
   {
        void *ctx = state->linalloc;
        ast_declaration *decl = new(ctx)  ast_declaration((yyvsp[0].identifier), NULL, NULL);
        decl->set_location((yylsp[0]));

        (yyval.subroutine_list) = (yyvsp[-2].subroutine_list);
        (yyval.subroutine_list)->declarations.push_tail(&decl->link);
   }
#line 4600 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 160: /* interpolation_qualifier: SMOOTH  */
#line 1970 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.smooth = 1;
   }
#line 4609 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 161: /* interpolation_qualifier: FLAT  */
#line 1975 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.flat = 1;
   }
#line 4618 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 162: /* interpolation_qualifier: NOPERSPECTIVE  */
#line 1980 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.noperspective = 1;
   }
#line 4627 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 163: /* type_qualifier: INVARIANT  */
#line 1989 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.invariant = 1;
   }
#line 4636 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 164: /* type_qualifier: PRECISE  */
#line 1994 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.precise = 1;
   }
#line 4645 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 171: /* type_qualifier: precision_qualifier  */
#line 2005 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(&(yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).precision = (yyvsp[0].n);
   }
#line 4654 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 172: /* type_qualifier: PRECISE type_qualifier  */
#line 2023 "../src/compiler/glsl/glsl_parser.yy"
   {
      if ((yyvsp[0].type_qualifier).flags.q.precise)
         _mesa_glsl_error(&(yylsp[-1]), state, "duplicate \"precise\" qualifier");

      (yyval.type_qualifier) = (yyvsp[0].type_qualifier);
      (yyval.type_qualifier).flags.q.precise = 1;
   }
#line 4666 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 173: /* type_qualifier: INVARIANT type_qualifier  */
#line 2031 "../src/compiler/glsl/glsl_parser.yy"
   {
      if ((yyvsp[0].type_qualifier).flags.q.invariant)
         _mesa_glsl_error(&(yylsp[-1]), state, "duplicate \"invariant\" qualifier");

      if (!state->has_420pack_or_es31() && (yyvsp[0].type_qualifier).flags.q.precise)
         _mesa_glsl_error(&(yylsp[-1]), state,
                          "\"invariant\" must come after \"precise\"");

      (yyval.type_qualifier) = (yyvsp[0].type_qualifier);
      (yyval.type_qualifier).flags.q.invariant = 1;

      /* GLSL ES 3.00 spec, section 4.6.1 "The Invariant Qualifier":
       *
       * "Only variables output from a shader can be candidates for invariance.
       * This includes user-defined output variables and the built-in output
       * variables. As only outputs can be declared as invariant, an invariant
       * output from one shader stage will still match an input of a subsequent
       * stage without the input being declared as invariant."
       *
       * On the desktop side, this text first appears in GLSL 4.20.
       */
      if (state->is_version(420, 300) && (yyval.type_qualifier).flags.q.in)
         _mesa_glsl_error(&(yylsp[-1]), state, "invariant qualifiers cannot be used with shader inputs");
   }
#line 4695 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 174: /* type_qualifier: interpolation_qualifier type_qualifier  */
#line 2056 "../src/compiler/glsl/glsl_parser.yy"
   {
      /* Section 4.3 of the GLSL 1.40 specification states:
       * "...qualified with one of these interpolation qualifiers"
       *
       * GLSL 1.30 claims to allow "one or more", but insists that:
       * "These interpolation qualifiers may only precede the qualifiers in,
       *  centroid in, out, or centroid out in a declaration."
       *
       * ...which means that e.g. smooth can't precede smooth, so there can be
       * only one after all, and the 1.40 text is a clarification, not a change.
       */
      if ((yyvsp[0].type_qualifier).has_interpolation())
         _mesa_glsl_error(&(yylsp[-1]), state, "duplicate interpolation qualifier");

      if (!state->has_420pack_or_es31() &&
          ((yyvsp[0].type_qualifier).flags.q.precise || (yyvsp[0].type_qualifier).flags.q.invariant)) {
         _mesa_glsl_error(&(yylsp[-1]), state, "interpolation qualifiers must come "
                          "after \"precise\" or \"invariant\"");
      }

      (yyval.type_qualifier) = (yyvsp[-1].type_qualifier);
      (yyval.type_qualifier).merge_qualifier(&(yylsp[-1]), state, (yyvsp[0].type_qualifier), false);
   }
#line 4723 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 175: /* type_qualifier: layout_qualifier type_qualifier  */
#line 2080 "../src/compiler/glsl/glsl_parser.yy"
   {
      /* In the absence of ARB_shading_language_420pack, layout qualifiers may
       * appear no later than auxiliary storage qualifiers. There is no
       * particularly clear spec language mandating this, but in all examples
       * the layout qualifier precedes the storage qualifier.
       *
       * We allow combinations of layout with interpolation, invariant or
       * precise qualifiers since these are useful in ARB_separate_shader_objects.
       * There is no clear spec guidance on this either.
       */
      (yyval.type_qualifier) = (yyvsp[-1].type_qualifier);
      (yyval.type_qualifier).merge_qualifier(& (yylsp[-1]), state, (yyvsp[0].type_qualifier), false, (yyvsp[0].type_qualifier).has_layout());
   }
#line 4741 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 176: /* type_qualifier: subroutine_qualifier type_qualifier  */
#line 2094 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.type_qualifier) = (yyvsp[-1].type_qualifier);
      (yyval.type_qualifier).merge_qualifier(&(yylsp[-1]), state, (yyvsp[0].type_qualifier), false);
   }
#line 4750 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 177: /* type_qualifier: auxiliary_storage_qualifier type_qualifier  */
#line 2099 "../src/compiler/glsl/glsl_parser.yy"
   {
      if ((yyvsp[0].type_qualifier).has_auxiliary_storage()) {
         _mesa_glsl_error(&(yylsp[-1]), state,
                          "duplicate auxiliary storage qualifier (centroid or sample)");
      }

      if ((!state->has_420pack_or_es31() && !state->EXT_gpu_shader4_enable) &&
          ((yyvsp[0].type_qualifier).flags.q.precise || (yyvsp[0].type_qualifier).flags.q.invariant ||
           (yyvsp[0].type_qualifier).has_interpolation() || (yyvsp[0].type_qualifier).has_layout())) {
         _mesa_glsl_error(&(yylsp[-1]), state, "auxiliary storage qualifiers must come "
                          "just before storage qualifiers");
      }
      (yyval.type_qualifier) = (yyvsp[-1].type_qualifier);
      (yyval.type_qualifier).merge_qualifier(&(yylsp[-1]), state, (yyvsp[0].type_qualifier), false);
   }
#line 4770 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 178: /* type_qualifier: storage_qualifier type_qualifier  */
#line 2115 "../src/compiler/glsl/glsl_parser.yy"
   {
      /* Section 4.3 of the GLSL 1.20 specification states:
       * "Variable declarations may have a storage qualifier specified..."
       *  1.30 clarifies this to "may have one storage qualifier".
       *
       * GL_EXT_gpu_shader4 allows "varying out" in fragment shaders.
       */
      if ((yyvsp[0].type_qualifier).has_storage() &&
          (!state->EXT_gpu_shader4_enable ||
           state->stage != MESA_SHADER_FRAGMENT ||
           !(yyvsp[-1].type_qualifier).flags.q.varying || !(yyvsp[0].type_qualifier).flags.q.out))
         _mesa_glsl_error(&(yylsp[-1]), state, "duplicate storage qualifier");

      if (!state->has_420pack_or_es31() &&
          ((yyvsp[0].type_qualifier).flags.q.precise || (yyvsp[0].type_qualifier).flags.q.invariant || (yyvsp[0].type_qualifier).has_interpolation() ||
           (yyvsp[0].type_qualifier).has_layout() || (yyvsp[0].type_qualifier).has_auxiliary_storage())) {
         _mesa_glsl_error(&(yylsp[-1]), state, "storage qualifiers must come after "
                          "precise, invariant, interpolation, layout and auxiliary "
                          "storage qualifiers");
      }

      (yyval.type_qualifier) = (yyvsp[-1].type_qualifier);
      (yyval.type_qualifier).merge_qualifier(&(yylsp[-1]), state, (yyvsp[0].type_qualifier), false);
   }
#line 4799 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 179: /* type_qualifier: precision_qualifier type_qualifier  */
#line 2140 "../src/compiler/glsl/glsl_parser.yy"
   {
      if ((yyvsp[0].type_qualifier).precision != ast_precision_none)
         _mesa_glsl_error(&(yylsp[-1]), state, "duplicate precision qualifier");

      if (!(state->has_420pack_or_es31()) &&
          (yyvsp[0].type_qualifier).flags.i != 0)
         _mesa_glsl_error(&(yylsp[-1]), state, "precision qualifiers must come last");

      (yyval.type_qualifier) = (yyvsp[0].type_qualifier);
      (yyval.type_qualifier).precision = (yyvsp[-1].n);
   }
#line 4815 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 180: /* type_qualifier: memory_qualifier type_qualifier  */
#line 2152 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.type_qualifier) = (yyvsp[-1].type_qualifier);
      (yyval.type_qualifier).merge_qualifier(&(yylsp[-1]), state, (yyvsp[0].type_qualifier), false);
   }
#line 4824 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 181: /* auxiliary_storage_qualifier: CENTROID  */
#line 2160 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.centroid = 1;
   }
#line 4833 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 182: /* auxiliary_storage_qualifier: SAMPLE  */
#line 2165 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.sample = 1;
   }
#line 4842 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 183: /* auxiliary_storage_qualifier: PATCH  */
#line 2170 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.patch = 1;
   }
#line 4851 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 184: /* storage_qualifier: CONST_TOK  */
#line 2177 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.constant = 1;
   }
#line 4860 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 185: /* storage_qualifier: ATTRIBUTE  */
#line 2182 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.attribute = 1;
   }
#line 4869 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 186: /* storage_qualifier: VARYING  */
#line 2187 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.varying = 1;
   }
#line 4878 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 187: /* storage_qualifier: IN_TOK  */
#line 2192 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.in = 1;
   }
#line 4887 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 188: /* storage_qualifier: OUT_TOK  */
#line 2197 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.out = 1;

      if (state->stage == MESA_SHADER_GEOMETRY &&
          state->has_explicit_attrib_stream()) {
         /* Section 4.3.8.2 (Output Layout Qualifiers) of the GLSL 4.00
          * spec says:
          *
          *     "If the block or variable is declared with the stream
          *     identifier, it is associated with the specified stream;
          *     otherwise, it is associated with the current default stream."
          */
          (yyval.type_qualifier).flags.q.stream = 1;
          (yyval.type_qualifier).flags.q.explicit_stream = 0;
          (yyval.type_qualifier).stream = state->out_qualifier->stream;
      }

      if (state->has_enhanced_layouts() && state->exts->ARB_transform_feedback3) {
          (yyval.type_qualifier).flags.q.xfb_buffer = 1;
          (yyval.type_qualifier).flags.q.explicit_xfb_buffer = 0;
          (yyval.type_qualifier).xfb_buffer = state->out_qualifier->xfb_buffer;
      }
   }
#line 4916 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 189: /* storage_qualifier: INOUT_TOK  */
#line 2222 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.in = 1;
      (yyval.type_qualifier).flags.q.out = 1;

      if (!state->has_framebuffer_fetch() ||
          !state->is_version(130, 300) ||
          state->stage != MESA_SHADER_FRAGMENT)
         _mesa_glsl_error(&(yylsp[0]), state, "A single interface variable cannot be "
                          "declared as both input and output");
   }
#line 4932 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 190: /* storage_qualifier: UNIFORM  */
#line 2234 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.uniform = 1;
   }
#line 4941 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 191: /* storage_qualifier: BUFFER  */
#line 2239 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.buffer = 1;
   }
#line 4950 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 192: /* storage_qualifier: SHARED  */
#line 2244 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.shared_storage = 1;
   }
#line 4959 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 193: /* memory_qualifier: COHERENT  */
#line 2252 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.coherent = 1;
   }
#line 4968 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 194: /* memory_qualifier: VOLATILE  */
#line 2257 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q._volatile = 1;
   }
#line 4977 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 195: /* memory_qualifier: RESTRICT  */
#line 2262 "../src/compiler/glsl/glsl_parser.yy"
   {
      STATIC_ASSERT(sizeof((yyval.type_qualifier).flags.q) <= sizeof((yyval.type_qualifier).flags.i));
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.restrict_flag = 1;
   }
#line 4987 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 196: /* memory_qualifier: READONLY  */
#line 2268 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.read_only = 1;
   }
#line 4996 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 197: /* memory_qualifier: WRITEONLY  */
#line 2273 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.write_only = 1;
   }
#line 5005 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 198: /* array_specifier: '[' ']'  */
#line 2281 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.array_specifier) = new(ctx) ast_array_specifier((yylsp[-1]), new(ctx) ast_expression(
                                                  ast_unsized_array_dim, NULL,
                                                  NULL, NULL));
      (yyval.array_specifier)->set_location_range((yylsp[-1]), (yylsp[0]));
   }
#line 5017 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 199: /* array_specifier: '[' constant_expression ']'  */
#line 2289 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.array_specifier) = new(ctx) ast_array_specifier((yylsp[-2]), (yyvsp[-1].expression));
      (yyval.array_specifier)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 5027 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 200: /* array_specifier: array_specifier '[' ']'  */
#line 2295 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.array_specifier) = (yyvsp[-2].array_specifier);

      if (state->check_arrays_of_arrays_allowed(& (yylsp[-2]))) {
         (yyval.array_specifier)->add_dimension(new(ctx) ast_expression(ast_unsized_array_dim, NULL,
                                                   NULL, NULL));
      }
   }
#line 5041 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 201: /* array_specifier: array_specifier '[' constant_expression ']'  */
#line 2305 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.array_specifier) = (yyvsp[-3].array_specifier);

      if (state->check_arrays_of_arrays_allowed(& (yylsp[-3]))) {
         (yyval.array_specifier)->add_dimension((yyvsp[-1].expression));
      }
   }
#line 5053 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 203: /* type_specifier: type_specifier_nonarray array_specifier  */
#line 2317 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.type_specifier) = (yyvsp[-1].type_specifier);
      (yyval.type_specifier)->array_specifier = (yyvsp[0].array_specifier);
   }
#line 5062 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 204: /* type_specifier_nonarray: basic_type_specifier_nonarray  */
#line 2325 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.type_specifier) = new(ctx) ast_type_specifier((yyvsp[0].type));
      (yyval.type_specifier)->set_location((yylsp[0]));
   }
#line 5072 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 205: /* type_specifier_nonarray: struct_specifier  */
#line 2331 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.type_specifier) = new(ctx) ast_type_specifier((yyvsp[0].struct_specifier));
      (yyval.type_specifier)->set_location((yylsp[0]));
   }
#line 5082 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 206: /* type_specifier_nonarray: TYPE_IDENTIFIER  */
#line 2337 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.type_specifier) = new(ctx) ast_type_specifier((yyvsp[0].identifier));
      (yyval.type_specifier)->set_location((yylsp[0]));
   }
#line 5092 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 207: /* basic_type_specifier_nonarray: VOID_TOK  */
#line 2345 "../src/compiler/glsl/glsl_parser.yy"
                            { (yyval.type) = glsl_type::void_type; }
#line 5098 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 208: /* basic_type_specifier_nonarray: BASIC_TYPE_TOK  */
#line 2346 "../src/compiler/glsl/glsl_parser.yy"
                            { (yyval.type) = (yyvsp[0].type); }
#line 5104 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 209: /* basic_type_specifier_nonarray: UNSIGNED BASIC_TYPE_TOK  */
#line 2348 "../src/compiler/glsl/glsl_parser.yy"
   {
      if ((yyvsp[0].type) == glsl_type::int_type) {
         (yyval.type) = glsl_type::uint_type;
      } else {
         _mesa_glsl_error(&(yylsp[-1]), state,
                          "\"unsigned\" is only allowed before \"int\"");
      }
   }
#line 5117 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 210: /* precision_qualifier: HIGHP  */
#line 2360 "../src/compiler/glsl/glsl_parser.yy"
   {
      state->check_precision_qualifiers_allowed(&(yylsp[0]));
      (yyval.n) = ast_precision_high;
   }
#line 5126 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 211: /* precision_qualifier: MEDIUMP  */
#line 2365 "../src/compiler/glsl/glsl_parser.yy"
   {
      state->check_precision_qualifiers_allowed(&(yylsp[0]));
      (yyval.n) = ast_precision_medium;
   }
#line 5135 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 212: /* precision_qualifier: LOWP  */
#line 2370 "../src/compiler/glsl/glsl_parser.yy"
   {
      state->check_precision_qualifiers_allowed(&(yylsp[0]));
      (yyval.n) = ast_precision_low;
   }
#line 5144 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 213: /* struct_specifier: STRUCT any_identifier '{' struct_declaration_list '}'  */
#line 2378 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.struct_specifier) = new(ctx) ast_struct_specifier((yyvsp[-3].identifier), (yyvsp[-1].declarator_list));
      (yyval.struct_specifier)->set_location_range((yylsp[-3]), (yylsp[0]));
      state->symbols->add_type((yyvsp[-3].identifier), glsl_type::void_type);
   }
#line 5155 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 214: /* struct_specifier: STRUCT '{' struct_declaration_list '}'  */
#line 2385 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;

      /* All anonymous structs have the same name. This simplifies matching of
       * globals whose type is an unnamed struct.
       *
       * It also avoids a memory leak when the same shader is compiled over and
       * over again.
       */
      (yyval.struct_specifier) = new(ctx) ast_struct_specifier("#anon_struct", (yyvsp[-1].declarator_list));

      (yyval.struct_specifier)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 5173 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 215: /* struct_declaration_list: struct_declaration  */
#line 2402 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.declarator_list) = (yyvsp[0].declarator_list);
      (yyvsp[0].declarator_list)->link.self_link();
   }
#line 5182 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 216: /* struct_declaration_list: struct_declaration_list struct_declaration  */
#line 2407 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.declarator_list) = (yyvsp[-1].declarator_list);
      (yyval.declarator_list)->link.insert_before(& (yyvsp[0].declarator_list)->link);
   }
#line 5191 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 217: /* struct_declaration: fully_specified_type struct_declarator_list ';'  */
#line 2415 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      ast_fully_specified_type *const type = (yyvsp[-2].fully_specified_type);
      type->set_location((yylsp[-2]));

      if (state->has_bindless()) {
         ast_type_qualifier input_layout_mask;

         /* Allow to declare qualifiers for images. */
         input_layout_mask.flags.i = 0;
         input_layout_mask.flags.q.coherent = 1;
         input_layout_mask.flags.q._volatile = 1;
         input_layout_mask.flags.q.restrict_flag = 1;
         input_layout_mask.flags.q.read_only = 1;
         input_layout_mask.flags.q.write_only = 1;
         input_layout_mask.flags.q.explicit_image_format = 1;

         if ((type->qualifier.flags.i & ~input_layout_mask.flags.i) != 0) {
            _mesa_glsl_error(&(yylsp[-2]), state,
                             "only precision and image qualifiers may be "
                             "applied to structure members");
         }
      } else {
         if (type->qualifier.flags.i != 0)
            _mesa_glsl_error(&(yylsp[-2]), state,
                             "only precision qualifiers may be applied to "
                             "structure members");
      }

      (yyval.declarator_list) = new(ctx) ast_declarator_list(type);
      (yyval.declarator_list)->set_location((yylsp[-1]));

      (yyval.declarator_list)->declarations.push_degenerate_list_at_head(& (yyvsp[-1].declaration)->link);
   }
#line 5230 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 218: /* struct_declarator_list: struct_declarator  */
#line 2453 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.declaration) = (yyvsp[0].declaration);
      (yyvsp[0].declaration)->link.self_link();
   }
#line 5239 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 219: /* struct_declarator_list: struct_declarator_list ',' struct_declarator  */
#line 2458 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.declaration) = (yyvsp[-2].declaration);
      (yyval.declaration)->link.insert_before(& (yyvsp[0].declaration)->link);
   }
#line 5248 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 220: /* struct_declarator: any_identifier  */
#line 2466 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.declaration) = new(ctx) ast_declaration((yyvsp[0].identifier), NULL, NULL);
      (yyval.declaration)->set_location((yylsp[0]));
   }
#line 5258 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 221: /* struct_declarator: any_identifier array_specifier  */
#line 2472 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.declaration) = new(ctx) ast_declaration((yyvsp[-1].identifier), (yyvsp[0].array_specifier), NULL);
      (yyval.declaration)->set_location_range((yylsp[-1]), (yylsp[0]));
   }
#line 5268 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 223: /* initializer: '{' initializer_list '}'  */
#line 2482 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.expression) = (yyvsp[-1].expression);
   }
#line 5276 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 224: /* initializer: '{' initializer_list ',' '}'  */
#line 2486 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.expression) = (yyvsp[-2].expression);
   }
#line 5284 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 225: /* initializer_list: initializer  */
#line 2493 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.expression) = new(ctx) ast_aggregate_initializer();
      (yyval.expression)->set_location((yylsp[0]));
      (yyval.expression)->expressions.push_tail(& (yyvsp[0].expression)->link);
   }
#line 5295 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 226: /* initializer_list: initializer_list ',' initializer  */
#line 2500 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyvsp[-2].expression)->expressions.push_tail(& (yyvsp[0].expression)->link);
   }
#line 5303 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 228: /* statement: compound_statement  */
#line 2512 "../src/compiler/glsl/glsl_parser.yy"
                             { (yyval.node) = (ast_node *) (yyvsp[0].compound_statement); }
#line 5309 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 237: /* compound_statement: '{' '}'  */
#line 2528 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.compound_statement) = new(ctx) ast_compound_statement(true, NULL);
      (yyval.compound_statement)->set_location_range((yylsp[-1]), (yylsp[0]));
   }
#line 5319 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 238: /* $@2: %empty  */
#line 2534 "../src/compiler/glsl/glsl_parser.yy"
   {
      state->symbols->push_scope();
   }
#line 5327 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 239: /* compound_statement: '{' $@2 statement_list '}'  */
#line 2538 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.compound_statement) = new(ctx) ast_compound_statement(true, (yyvsp[-1].node));
      (yyval.compound_statement)->set_location_range((yylsp[-3]), (yylsp[0]));
      state->symbols->pop_scope();
   }
#line 5338 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 240: /* statement_no_new_scope: compound_statement_no_new_scope  */
#line 2547 "../src/compiler/glsl/glsl_parser.yy"
                                   { (yyval.node) = (ast_node *) (yyvsp[0].compound_statement); }
#line 5344 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 242: /* compound_statement_no_new_scope: '{' '}'  */
#line 2553 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.compound_statement) = new(ctx) ast_compound_statement(false, NULL);
      (yyval.compound_statement)->set_location_range((yylsp[-1]), (yylsp[0]));
   }
#line 5354 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 243: /* compound_statement_no_new_scope: '{' statement_list '}'  */
#line 2559 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.compound_statement) = new(ctx) ast_compound_statement(false, (yyvsp[-1].node));
      (yyval.compound_statement)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 5364 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 244: /* statement_list: statement  */
#line 2568 "../src/compiler/glsl/glsl_parser.yy"
   {
      if ((yyvsp[0].node) == NULL) {
         _mesa_glsl_error(& (yylsp[0]), state, "<nil> statement");
         assert((yyvsp[0].node) != NULL);
      }

      (yyval.node) = (yyvsp[0].node);
      (yyval.node)->link.self_link();
   }
#line 5378 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 245: /* statement_list: statement_list statement  */
#line 2578 "../src/compiler/glsl/glsl_parser.yy"
   {
      if ((yyvsp[0].node) == NULL) {
         _mesa_glsl_error(& (yylsp[0]), state, "<nil> statement");
         assert((yyvsp[0].node) != NULL);
      }
      (yyval.node) = (yyvsp[-1].node);
      (yyval.node)->link.insert_before(& (yyvsp[0].node)->link);
   }
#line 5391 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 246: /* statement_list: statement_list extension_statement  */
#line 2587 "../src/compiler/glsl/glsl_parser.yy"
   {
      if (!state->allow_extension_directive_midshader) {
         _mesa_glsl_error(& (yylsp[-1]), state,
                          "#extension directive is not allowed "
                          "in the middle of a shader");
         YYERROR;
      }
   }
#line 5404 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 247: /* expression_statement: ';'  */
#line 2599 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.node) = new(ctx) ast_expression_statement(NULL);
      (yyval.node)->set_location((yylsp[0]));
   }
#line 5414 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 248: /* expression_statement: expression ';'  */
#line 2605 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.node) = new(ctx) ast_expression_statement((yyvsp[-1].expression));
      (yyval.node)->set_location((yylsp[-1]));
   }
#line 5424 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 249: /* selection_statement: IF '(' expression ')' selection_rest_statement  */
#line 2614 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.node) = new(state->linalloc) ast_selection_statement((yyvsp[-2].expression), (yyvsp[0].selection_rest_statement).then_statement,
                                                        (yyvsp[0].selection_rest_statement).else_statement);
      (yyval.node)->set_location_range((yylsp[-4]), (yylsp[0]));
   }
#line 5434 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 250: /* selection_rest_statement: statement ELSE statement  */
#line 2623 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.selection_rest_statement).then_statement = (yyvsp[-2].node);
      (yyval.selection_rest_statement).else_statement = (yyvsp[0].node);
   }
#line 5443 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 251: /* selection_rest_statement: statement  */
#line 2628 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.selection_rest_statement).then_statement = (yyvsp[0].node);
      (yyval.selection_rest_statement).else_statement = NULL;
   }
#line 5452 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 252: /* condition: expression  */
#line 2636 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.node) = (ast_node *) (yyvsp[0].expression);
   }
#line 5460 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 253: /* condition: fully_specified_type any_identifier '=' initializer  */
#line 2640 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      ast_declaration *decl = new(ctx) ast_declaration((yyvsp[-2].identifier), NULL, (yyvsp[0].expression));
      ast_declarator_list *declarator = new(ctx) ast_declarator_list((yyvsp[-3].fully_specified_type));
      decl->set_location_range((yylsp[-2]), (yylsp[0]));
      declarator->set_location((yylsp[-3]));

      declarator->declarations.push_tail(&decl->link);
      (yyval.node) = declarator;
   }
#line 5475 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 254: /* switch_statement: SWITCH '(' expression ')' switch_body  */
#line 2658 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.node) = new(state->linalloc) ast_switch_statement((yyvsp[-2].expression), (yyvsp[0].switch_body));
      (yyval.node)->set_location_range((yylsp[-4]), (yylsp[0]));
   }
#line 5484 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 255: /* switch_body: '{' '}'  */
#line 2666 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.switch_body) = new(state->linalloc) ast_switch_body(NULL);
      (yyval.switch_body)->set_location_range((yylsp[-1]), (yylsp[0]));
   }
#line 5493 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 256: /* switch_body: '{' case_statement_list '}'  */
#line 2671 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.switch_body) = new(state->linalloc) ast_switch_body((yyvsp[-1].case_statement_list));
      (yyval.switch_body)->set_location_range((yylsp[-2]), (yylsp[0]));
   }
#line 5502 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 257: /* case_label: CASE expression ':'  */
#line 2679 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.case_label) = new(state->linalloc) ast_case_label((yyvsp[-1].expression));
      (yyval.case_label)->set_location((yylsp[-1]));
   }
#line 5511 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 258: /* case_label: DEFAULT ':'  */
#line 2684 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.case_label) = new(state->linalloc) ast_case_label(NULL);
      (yyval.case_label)->set_location((yylsp[0]));
   }
#line 5520 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 259: /* case_label_list: case_label  */
#line 2692 "../src/compiler/glsl/glsl_parser.yy"
   {
      ast_case_label_list *labels = new(state->linalloc) ast_case_label_list();

      labels->labels.push_tail(& (yyvsp[0].case_label)->link);
      (yyval.case_label_list) = labels;
      (yyval.case_label_list)->set_location((yylsp[0]));
   }
#line 5532 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 260: /* case_label_list: case_label_list case_label  */
#line 2700 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.case_label_list) = (yyvsp[-1].case_label_list);
      (yyval.case_label_list)->labels.push_tail(& (yyvsp[0].case_label)->link);
   }
#line 5541 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 261: /* case_statement: case_label_list statement  */
#line 2708 "../src/compiler/glsl/glsl_parser.yy"
   {
      ast_case_statement *stmts = new(state->linalloc) ast_case_statement((yyvsp[-1].case_label_list));
      stmts->set_location((yylsp[0]));

      stmts->stmts.push_tail(& (yyvsp[0].node)->link);
      (yyval.case_statement) = stmts;
   }
#line 5553 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 262: /* case_statement: case_statement statement  */
#line 2716 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.case_statement) = (yyvsp[-1].case_statement);
      (yyval.case_statement)->stmts.push_tail(& (yyvsp[0].node)->link);
   }
#line 5562 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 263: /* case_statement_list: case_statement  */
#line 2724 "../src/compiler/glsl/glsl_parser.yy"
   {
      ast_case_statement_list *cases= new(state->linalloc) ast_case_statement_list();
      cases->set_location((yylsp[0]));

      cases->cases.push_tail(& (yyvsp[0].case_statement)->link);
      (yyval.case_statement_list) = cases;
   }
#line 5574 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 264: /* case_statement_list: case_statement_list case_statement  */
#line 2732 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.case_statement_list) = (yyvsp[-1].case_statement_list);
      (yyval.case_statement_list)->cases.push_tail(& (yyvsp[0].case_statement)->link);
   }
#line 5583 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 265: /* iteration_statement: WHILE '(' condition ')' statement_no_new_scope  */
#line 2740 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.node) = new(ctx) ast_iteration_statement(ast_iteration_statement::ast_while,
                                            NULL, (yyvsp[-2].node), NULL, (yyvsp[0].node));
      (yyval.node)->set_location_range((yylsp[-4]), (yylsp[-1]));
   }
#line 5594 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 266: /* iteration_statement: DO statement_no_new_scope WHILE '(' expression ')' ';'  */
#line 2747 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.node) = new(ctx) ast_iteration_statement(ast_iteration_statement::ast_do_while,
                                            NULL, (yyvsp[-2].expression), NULL, (yyvsp[-5].node));
      (yyval.node)->set_location_range((yylsp[-6]), (yylsp[-1]));
   }
#line 5605 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 267: /* iteration_statement: FOR '(' for_init_statement for_rest_statement ')' statement_no_new_scope  */
#line 2754 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.node) = new(ctx) ast_iteration_statement(ast_iteration_statement::ast_for,
                                            (yyvsp[-3].node), (yyvsp[-2].for_rest_statement).cond, (yyvsp[-2].for_rest_statement).rest, (yyvsp[0].node));
      (yyval.node)->set_location_range((yylsp[-5]), (yylsp[0]));
   }
#line 5616 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 271: /* conditionopt: %empty  */
#line 2770 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.node) = NULL;
   }
#line 5624 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 272: /* for_rest_statement: conditionopt ';'  */
#line 2777 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.for_rest_statement).cond = (yyvsp[-1].node);
      (yyval.for_rest_statement).rest = NULL;
   }
#line 5633 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 273: /* for_rest_statement: conditionopt ';' expression  */
#line 2782 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.for_rest_statement).cond = (yyvsp[-2].node);
      (yyval.for_rest_statement).rest = (yyvsp[0].expression);
   }
#line 5642 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 274: /* jump_statement: CONTINUE ';'  */
#line 2791 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.node) = new(ctx) ast_jump_statement(ast_jump_statement::ast_continue, NULL);
      (yyval.node)->set_location((yylsp[-1]));
   }
#line 5652 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 275: /* jump_statement: BREAK ';'  */
#line 2797 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.node) = new(ctx) ast_jump_statement(ast_jump_statement::ast_break, NULL);
      (yyval.node)->set_location((yylsp[-1]));
   }
#line 5662 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 276: /* jump_statement: RETURN ';'  */
#line 2803 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.node) = new(ctx) ast_jump_statement(ast_jump_statement::ast_return, NULL);
      (yyval.node)->set_location((yylsp[-1]));
   }
#line 5672 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 277: /* jump_statement: RETURN expression ';'  */
#line 2809 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.node) = new(ctx) ast_jump_statement(ast_jump_statement::ast_return, (yyvsp[-1].expression));
      (yyval.node)->set_location_range((yylsp[-2]), (yylsp[-1]));
   }
#line 5682 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 278: /* jump_statement: DISCARD ';'  */
#line 2815 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.node) = new(ctx) ast_jump_statement(ast_jump_statement::ast_discard, NULL);
      (yyval.node)->set_location((yylsp[-1]));
   }
#line 5692 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 279: /* demote_statement: DEMOTE ';'  */
#line 2824 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.node) = new(ctx) ast_demote_statement();
      (yyval.node)->set_location((yylsp[-1]));
   }
#line 5702 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 280: /* external_declaration: function_definition  */
#line 2832 "../src/compiler/glsl/glsl_parser.yy"
                            { (yyval.node) = (yyvsp[0].function_definition); }
#line 5708 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 281: /* external_declaration: declaration  */
#line 2833 "../src/compiler/glsl/glsl_parser.yy"
                            { (yyval.node) = (yyvsp[0].node); }
#line 5714 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 282: /* external_declaration: pragma_statement  */
#line 2834 "../src/compiler/glsl/glsl_parser.yy"
                            { (yyval.node) = (yyvsp[0].node); }
#line 5720 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 283: /* external_declaration: layout_defaults  */
#line 2835 "../src/compiler/glsl/glsl_parser.yy"
                            { (yyval.node) = (yyvsp[0].node); }
#line 5726 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 284: /* external_declaration: ';'  */
#line 2836 "../src/compiler/glsl/glsl_parser.yy"
                            { (yyval.node) = NULL; }
#line 5732 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 285: /* function_definition: function_prototype compound_statement_no_new_scope  */
#line 2841 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      (yyval.function_definition) = new(ctx) ast_function_definition();
      (yyval.function_definition)->set_location_range((yylsp[-1]), (yylsp[0]));
      (yyval.function_definition)->prototype = (yyvsp[-1].function);
      (yyval.function_definition)->body = (yyvsp[0].compound_statement);

      state->symbols->pop_scope();
   }
#line 5746 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 286: /* interface_block: basic_interface_block  */
#line 2855 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.node) = (yyvsp[0].interface_block);
   }
#line 5754 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 287: /* interface_block: layout_qualifier interface_block  */
#line 2859 "../src/compiler/glsl/glsl_parser.yy"
   {
      ast_interface_block *block = (ast_interface_block *) (yyvsp[0].node);

      if (!(yyvsp[-1].type_qualifier).merge_qualifier(& (yylsp[-1]), state, block->layout, false,
                              block->layout.has_layout())) {
         YYERROR;
      }

      block->layout = (yyvsp[-1].type_qualifier);

      (yyval.node) = block;
   }
#line 5771 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 288: /* interface_block: memory_qualifier interface_block  */
#line 2872 "../src/compiler/glsl/glsl_parser.yy"
   {
      ast_interface_block *block = (ast_interface_block *)(yyvsp[0].node);

      if (!block->default_layout.flags.q.buffer) {
            _mesa_glsl_error(& (yylsp[-1]), state,
                             "memory qualifiers can only be used in the "
                             "declaration of shader storage blocks");
      }
      if (!(yyvsp[-1].type_qualifier).merge_qualifier(& (yylsp[-1]), state, block->layout, false)) {
         YYERROR;
      }
      block->layout = (yyvsp[-1].type_qualifier);
      (yyval.node) = block;
   }
#line 5790 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 289: /* basic_interface_block: interface_qualifier NEW_IDENTIFIER '{' member_list '}' instance_name_opt ';'  */
#line 2890 "../src/compiler/glsl/glsl_parser.yy"
   {
      ast_interface_block *const block = (yyvsp[-1].interface_block);

      if ((yyvsp[-6].type_qualifier).flags.q.uniform) {
         block->default_layout = *state->default_uniform_qualifier;
      } else if ((yyvsp[-6].type_qualifier).flags.q.buffer) {
         block->default_layout = *state->default_shader_storage_qualifier;
      }
      block->block_name = (yyvsp[-5].identifier);
      block->declarations.push_degenerate_list_at_head(& (yyvsp[-3].declarator_list)->link);

      _mesa_ast_process_interface_block(& (yylsp[-6]), state, block, (yyvsp[-6].type_qualifier));

      (yyval.interface_block) = block;
   }
#line 5810 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 290: /* interface_qualifier: IN_TOK  */
#line 2909 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.in = 1;
   }
#line 5819 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 291: /* interface_qualifier: OUT_TOK  */
#line 2914 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.out = 1;
   }
#line 5828 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 292: /* interface_qualifier: UNIFORM  */
#line 2919 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.uniform = 1;
   }
#line 5837 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 293: /* interface_qualifier: BUFFER  */
#line 2924 "../src/compiler/glsl/glsl_parser.yy"
   {
      memset(& (yyval.type_qualifier), 0, sizeof((yyval.type_qualifier)));
      (yyval.type_qualifier).flags.q.buffer = 1;
   }
#line 5846 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 294: /* interface_qualifier: auxiliary_storage_qualifier interface_qualifier  */
#line 2929 "../src/compiler/glsl/glsl_parser.yy"
   {
      if (!(yyvsp[-1].type_qualifier).flags.q.patch) {
         _mesa_glsl_error(&(yylsp[-1]), state, "invalid interface qualifier");
      }
      if ((yyvsp[0].type_qualifier).has_auxiliary_storage()) {
         _mesa_glsl_error(&(yylsp[-1]), state, "duplicate patch qualifier");
      }
      (yyval.type_qualifier) = (yyvsp[0].type_qualifier);
      (yyval.type_qualifier).flags.q.patch = 1;
   }
#line 5861 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 295: /* instance_name_opt: %empty  */
#line 2943 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.interface_block) = new(state->linalloc) ast_interface_block(NULL, NULL);
   }
#line 5869 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 296: /* instance_name_opt: NEW_IDENTIFIER  */
#line 2947 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.interface_block) = new(state->linalloc) ast_interface_block((yyvsp[0].identifier), NULL);
      (yyval.interface_block)->set_location((yylsp[0]));
   }
#line 5878 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 297: /* instance_name_opt: NEW_IDENTIFIER array_specifier  */
#line 2952 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.interface_block) = new(state->linalloc) ast_interface_block((yyvsp[-1].identifier), (yyvsp[0].array_specifier));
      (yyval.interface_block)->set_location_range((yylsp[-1]), (yylsp[0]));
   }
#line 5887 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 298: /* member_list: member_declaration  */
#line 2960 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.declarator_list) = (yyvsp[0].declarator_list);
      (yyvsp[0].declarator_list)->link.self_link();
   }
#line 5896 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 299: /* member_list: member_declaration member_list  */
#line 2965 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.declarator_list) = (yyvsp[-1].declarator_list);
      (yyvsp[0].declarator_list)->link.insert_before(& (yyval.declarator_list)->link);
   }
#line 5905 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 300: /* member_declaration: fully_specified_type struct_declarator_list ';'  */
#line 2973 "../src/compiler/glsl/glsl_parser.yy"
   {
      void *ctx = state->linalloc;
      ast_fully_specified_type *type = (yyvsp[-2].fully_specified_type);
      type->set_location((yylsp[-2]));

      if (type->qualifier.flags.q.attribute) {
         _mesa_glsl_error(& (yylsp[-2]), state,
                          "keyword 'attribute' cannot be used with "
                          "interface block member");
      } else if (type->qualifier.flags.q.varying) {
         _mesa_glsl_error(& (yylsp[-2]), state,
                          "keyword 'varying' cannot be used with "
                          "interface block member");
      }

      (yyval.declarator_list) = new(ctx) ast_declarator_list(type);
      (yyval.declarator_list)->set_location((yylsp[-1]));

      (yyval.declarator_list)->declarations.push_degenerate_list_at_head(& (yyvsp[-1].declaration)->link);
   }
#line 5930 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 301: /* layout_uniform_defaults: layout_qualifier layout_uniform_defaults  */
#line 2997 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.type_qualifier) = (yyvsp[-1].type_qualifier);
      if (!(yyval.type_qualifier).merge_qualifier(& (yylsp[-1]), state, (yyvsp[0].type_qualifier), false, true)) {
         YYERROR;
      }
   }
#line 5941 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 303: /* layout_buffer_defaults: layout_qualifier layout_buffer_defaults  */
#line 3008 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.type_qualifier) = (yyvsp[-1].type_qualifier);
      if (!(yyval.type_qualifier).merge_qualifier(& (yylsp[-1]), state, (yyvsp[0].type_qualifier), false, true)) {
         YYERROR;
      }
   }
#line 5952 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 305: /* layout_in_defaults: layout_qualifier layout_in_defaults  */
#line 3019 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.type_qualifier) = (yyvsp[-1].type_qualifier);
      if (!(yyval.type_qualifier).merge_qualifier(& (yylsp[-1]), state, (yyvsp[0].type_qualifier), false, true)) {
         YYERROR;
      }
      if (!(yyval.type_qualifier).validate_in_qualifier(& (yylsp[-1]), state)) {
         YYERROR;
      }
   }
#line 5966 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 306: /* layout_in_defaults: layout_qualifier IN_TOK ';'  */
#line 3029 "../src/compiler/glsl/glsl_parser.yy"
   {
      if (!(yyvsp[-2].type_qualifier).validate_in_qualifier(& (yylsp[-2]), state)) {
         YYERROR;
      }
   }
#line 5976 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 307: /* layout_out_defaults: layout_qualifier layout_out_defaults  */
#line 3038 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.type_qualifier) = (yyvsp[-1].type_qualifier);
      if (!(yyval.type_qualifier).merge_qualifier(& (yylsp[-1]), state, (yyvsp[0].type_qualifier), false, true)) {
         YYERROR;
      }
      if (!(yyval.type_qualifier).validate_out_qualifier(& (yylsp[-1]), state)) {
         YYERROR;
      }
   }
#line 5990 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 308: /* layout_out_defaults: layout_qualifier OUT_TOK ';'  */
#line 3048 "../src/compiler/glsl/glsl_parser.yy"
   {
      if (!(yyvsp[-2].type_qualifier).validate_out_qualifier(& (yylsp[-2]), state)) {
         YYERROR;
      }
   }
#line 6000 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 309: /* layout_defaults: layout_uniform_defaults  */
#line 3057 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.node) = NULL;
      if (!state->default_uniform_qualifier->
             merge_qualifier(& (yylsp[0]), state, (yyvsp[0].type_qualifier), false)) {
         YYERROR;
      }
      if (!state->default_uniform_qualifier->
             push_to_global(& (yylsp[0]), state)) {
         YYERROR;
      }
   }
#line 6016 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 310: /* layout_defaults: layout_buffer_defaults  */
#line 3069 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.node) = NULL;
      if (!state->default_shader_storage_qualifier->
             merge_qualifier(& (yylsp[0]), state, (yyvsp[0].type_qualifier), false)) {
         YYERROR;
      }
      if (!state->default_shader_storage_qualifier->
             push_to_global(& (yylsp[0]), state)) {
         YYERROR;
      }

      /* From the GLSL 4.50 spec, section 4.4.5:
       *
       *     "It is a compile-time error to specify the binding identifier for
       *     the global scope or for block member declarations."
       */
      if (state->default_shader_storage_qualifier->flags.q.explicit_binding) {
         _mesa_glsl_error(& (yylsp[0]), state,
                          "binding qualifier cannot be set for default layout");
      }
   }
#line 6042 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 311: /* layout_defaults: layout_in_defaults  */
#line 3091 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.node) = NULL;
      if (!(yyvsp[0].type_qualifier).merge_into_in_qualifier(& (yylsp[0]), state, (yyval.node))) {
         YYERROR;
      }
      if (!state->in_qualifier->push_to_global(& (yylsp[0]), state)) {
         YYERROR;
      }
   }
#line 6056 "src/compiler/glsl/glsl_parser.cpp"
    break;

  case 312: /* layout_defaults: layout_out_defaults  */
#line 3101 "../src/compiler/glsl/glsl_parser.yy"
   {
      (yyval.node) = NULL;
      if (!(yyvsp[0].type_qualifier).merge_into_out_qualifier(& (yylsp[0]), state, (yyval.node))) {
         YYERROR;
      }
      if (!state->out_qualifier->push_to_global(& (yylsp[0]), state)) {
         YYERROR;
      }

      (void)yynerrs;
   }
#line 6072 "src/compiler/glsl/glsl_parser.cpp"
    break;


#line 6076 "src/compiler/glsl/glsl_parser.cpp"

      default: break;
    }
  /* User semantic actions sometimes alter yychar, and that requires
     that yytoken be updated with the new translation.  We take the
     approach of translating immediately before every use of yytoken.
     One alternative is translating here after every semantic action,
     but that translation would be missed if the semantic action invokes
     YYABORT, YYACCEPT, or YYERROR immediately after altering yychar or
     if it invokes YYBACKUP.  In the case of YYABORT or YYACCEPT, an
     incorrect destructor might then be invoked immediately.  In the
     case of YYERROR or YYBACKUP, subsequent parser actions might lead
     to an incorrect destructor call or verbose syntax error message
     before the lookahead is translated.  */
  YY_SYMBOL_PRINT ("-> $$ =", YY_CAST (yysymbol_kind_t, yyr1[yyn]), &yyval, &yyloc);

  YYPOPSTACK (yylen);
  yylen = 0;

  *++yyvsp = yyval;
  *++yylsp = yyloc;

  /* Now 'shift' the result of the reduction.  Determine what state
     that goes to, based on the state we popped back to and the rule
     number reduced by.  */
  {
    const int yylhs = yyr1[yyn] - YYNTOKENS;
    const int yyi = yypgoto[yylhs] + *yyssp;
    yystate = (0 <= yyi && yyi <= YYLAST && yycheck[yyi] == *yyssp
               ? yytable[yyi]
               : yydefgoto[yylhs]);
  }

  goto yynewstate;


/*--------------------------------------.
| yyerrlab -- here on detecting error.  |
`--------------------------------------*/
yyerrlab:
  /* Make sure we have latest lookahead translation.  See comments at
     user semantic actions for why this is necessary.  */
  yytoken = yychar == YYEMPTY ? YYSYMBOL_YYEMPTY : YYTRANSLATE (yychar);
  /* If not already recovering from an error, report this error.  */
  if (!yyerrstatus)
    {
      ++yynerrs;
      {
        yypcontext_t yyctx
          = {yyssp, yytoken, &yylloc};
        char const *yymsgp = YY_("syntax error");
        int yysyntax_error_status;
        yysyntax_error_status = yysyntax_error (&yymsg_alloc, &yymsg, &yyctx);
        if (yysyntax_error_status == 0)
          yymsgp = yymsg;
        else if (yysyntax_error_status == -1)
          {
            if (yymsg != yymsgbuf)
              YYSTACK_FREE (yymsg);
            yymsg = YY_CAST (char *,
                             YYSTACK_ALLOC (YY_CAST (YYSIZE_T, yymsg_alloc)));
            if (yymsg)
              {
                yysyntax_error_status
                  = yysyntax_error (&yymsg_alloc, &yymsg, &yyctx);
                yymsgp = yymsg;
              }
            else
              {
                yymsg = yymsgbuf;
                yymsg_alloc = sizeof yymsgbuf;
                yysyntax_error_status = YYENOMEM;
              }
          }
        yyerror (&yylloc, state, yymsgp);
        if (yysyntax_error_status == YYENOMEM)
          YYNOMEM;
      }
    }

  yyerror_range[1] = yylloc;
  if (yyerrstatus == 3)
    {
      /* If just tried and failed to reuse lookahead token after an
         error, discard it.  */

      if (yychar <= YYEOF)
        {
          /* Return failure if at end of input.  */
          if (yychar == YYEOF)
            YYABORT;
        }
      else
        {
          yydestruct ("Error: discarding",
                      yytoken, &yylval, &yylloc, state);
          yychar = YYEMPTY;
        }
    }

  /* Else will try to reuse lookahead token after shifting the error
     token.  */
  goto yyerrlab1;


/*---------------------------------------------------.
| yyerrorlab -- error raised explicitly by YYERROR.  |
`---------------------------------------------------*/
yyerrorlab:
  /* Pacify compilers when the user code never invokes YYERROR and the
     label yyerrorlab therefore never appears in user code.  */
  if (0)
    YYERROR;
  ++yynerrs;

  /* Do not reclaim the symbols of the rule whose action triggered
     this YYERROR.  */
  YYPOPSTACK (yylen);
  yylen = 0;
  YY_STACK_PRINT (yyss, yyssp);
  yystate = *yyssp;
  goto yyerrlab1;


/*-------------------------------------------------------------.
| yyerrlab1 -- common code for both syntax error and YYERROR.  |
`-------------------------------------------------------------*/
yyerrlab1:
  yyerrstatus = 3;      /* Each real token shifted decrements this.  */

  /* Pop stack until we find a state that shifts the error token.  */
  for (;;)
    {
      yyn = yypact[yystate];
      if (!yypact_value_is_default (yyn))
        {
          yyn += YYSYMBOL_YYerror;
          if (0 <= yyn && yyn <= YYLAST && yycheck[yyn] == YYSYMBOL_YYerror)
            {
              yyn = yytable[yyn];
              if (0 < yyn)
                break;
            }
        }

      /* Pop the current state because it cannot handle the error token.  */
      if (yyssp == yyss)
        YYABORT;

      yyerror_range[1] = *yylsp;
      yydestruct ("Error: popping",
                  YY_ACCESSING_SYMBOL (yystate), yyvsp, yylsp, state);
      YYPOPSTACK (1);
      yystate = *yyssp;
      YY_STACK_PRINT (yyss, yyssp);
    }

  YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
  *++yyvsp = yylval;
  YY_IGNORE_MAYBE_UNINITIALIZED_END

  yyerror_range[2] = yylloc;
  ++yylsp;
  YYLLOC_DEFAULT (*yylsp, yyerror_range, 2);

  /* Shift the error token.  */
  YY_SYMBOL_PRINT ("Shifting", YY_ACCESSING_SYMBOL (yyn), yyvsp, yylsp);

  yystate = yyn;
  goto yynewstate;


/*-------------------------------------.
| yyacceptlab -- YYACCEPT comes here.  |
`-------------------------------------*/
yyacceptlab:
  yyresult = 0;
  goto yyreturnlab;


/*-----------------------------------.
| yyabortlab -- YYABORT comes here.  |
`-----------------------------------*/
yyabortlab:
  yyresult = 1;
  goto yyreturnlab;


/*-----------------------------------------------------------.
| yyexhaustedlab -- YYNOMEM (memory exhaustion) comes here.  |
`-----------------------------------------------------------*/
yyexhaustedlab:
  yyerror (&yylloc, state, YY_("memory exhausted"));
  yyresult = 2;
  goto yyreturnlab;


/*----------------------------------------------------------.
| yyreturnlab -- parsing is finished, clean up and return.  |
`----------------------------------------------------------*/
yyreturnlab:
  if (yychar != YYEMPTY)
    {
      /* Make sure we have latest lookahead translation.  See comments at
         user semantic actions for why this is necessary.  */
      yytoken = YYTRANSLATE (yychar);
      yydestruct ("Cleanup: discarding lookahead",
                  yytoken, &yylval, &yylloc, state);
    }
  /* Do not reclaim the symbols of the rule whose action triggered
     this YYABORT or YYACCEPT.  */
  YYPOPSTACK (yylen);
  YY_STACK_PRINT (yyss, yyssp);
  while (yyssp != yyss)
    {
      yydestruct ("Cleanup: popping",
                  YY_ACCESSING_SYMBOL (+*yyssp), yyvsp, yylsp, state);
      YYPOPSTACK (1);
    }
#ifndef yyoverflow
  if (yyss != yyssa)
    YYSTACK_FREE (yyss);
#endif
  if (yymsg != yymsgbuf)
    YYSTACK_FREE (yymsg);
  return yyresult;
}

