/* A Bison parser, made by GNU Bison 3.8.2.  */

/* Bison interface for Yacc-like parsers in C

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

/* DO NOT RELY ON FEATURES THAT ARE NOT DOCUMENTED in the manual,
   especially those whose name start with YY_ or yy_.  They are
   private implementation details that can be changed or removed.  */

#ifndef YY__MESA_GLSL_SRC_COMPILER_GLSL_GLSL_PARSER_H_INCLUDED
# define YY__MESA_GLSL_SRC_COMPILER_GLSL_GLSL_PARSER_H_INCLUDED
/* Debug traces.  */
#ifndef YYDEBUG
# define YYDEBUG 0
#endif
#if YYDEBUG
extern int _mesa_glsl_debug;
#endif

/* Token kinds.  */
#ifndef YYTOKENTYPE
# define YYTOKENTYPE
  enum yytokentype
  {
    YYEMPTY = -2,
    YYEOF = 0,                     /* "end of file"  */
    YYerror = 256,                 /* error  */
    YYUNDEF = 257,                 /* "invalid token"  */
    ATTRIBUTE = 258,               /* ATTRIBUTE  */
    CONST_TOK = 259,               /* CONST_TOK  */
    BASIC_TYPE_TOK = 260,          /* BASIC_TYPE_TOK  */
    BREAK = 261,                   /* BREAK  */
    BUFFER = 262,                  /* BUFFER  */
    CONTINUE = 263,                /* CONTINUE  */
    DO = 264,                      /* DO  */
    ELSE = 265,                    /* ELSE  */
    FOR = 266,                     /* FOR  */
    IF = 267,                      /* IF  */
    DEMOTE = 268,                  /* DEMOTE  */
    DISCARD = 269,                 /* DISCARD  */
    RETURN = 270,                  /* RETURN  */
    SWITCH = 271,                  /* SWITCH  */
    CASE = 272,                    /* CASE  */
    DEFAULT = 273,                 /* DEFAULT  */
    CENTROID = 274,                /* CENTROID  */
    IN_TOK = 275,                  /* IN_TOK  */
    OUT_TOK = 276,                 /* OUT_TOK  */
    INOUT_TOK = 277,               /* INOUT_TOK  */
    UNIFORM = 278,                 /* UNIFORM  */
    VARYING = 279,                 /* VARYING  */
    SAMPLE = 280,                  /* SAMPLE  */
    NOPERSPECTIVE = 281,           /* NOPERSPECTIVE  */
    FLAT = 282,                    /* FLAT  */
    SMOOTH = 283,                  /* SMOOTH  */
    IMAGE1DSHADOW = 284,           /* IMAGE1DSHADOW  */
    IMAGE2DSHADOW = 285,           /* IMAGE2DSHADOW  */
    IMAGE1DARRAYSHADOW = 286,      /* IMAGE1DARRAYSHADOW  */
    IMAGE2DARRAYSHADOW = 287,      /* IMAGE2DARRAYSHADOW  */
    COHERENT = 288,                /* COHERENT  */
    VOLATILE = 289,                /* VOLATILE  */
    RESTRICT = 290,                /* RESTRICT  */
    READONLY = 291,                /* READONLY  */
    WRITEONLY = 292,               /* WRITEONLY  */
    SHARED = 293,                  /* SHARED  */
    STRUCT = 294,                  /* STRUCT  */
    VOID_TOK = 295,                /* VOID_TOK  */
    WHILE = 296,                   /* WHILE  */
    IDENTIFIER = 297,              /* IDENTIFIER  */
    TYPE_IDENTIFIER = 298,         /* TYPE_IDENTIFIER  */
    NEW_IDENTIFIER = 299,          /* NEW_IDENTIFIER  */
    FLOATCONSTANT = 300,           /* FLOATCONSTANT  */
    DOUBLECONSTANT = 301,          /* DOUBLECONSTANT  */
    INTCONSTANT = 302,             /* INTCONSTANT  */
    UINTCONSTANT = 303,            /* UINTCONSTANT  */
    BOOLCONSTANT = 304,            /* BOOLCONSTANT  */
    INT64CONSTANT = 305,           /* INT64CONSTANT  */
    UINT64CONSTANT = 306,          /* UINT64CONSTANT  */
    FIELD_SELECTION = 307,         /* FIELD_SELECTION  */
    LEFT_OP = 308,                 /* LEFT_OP  */
    RIGHT_OP = 309,                /* RIGHT_OP  */
    INC_OP = 310,                  /* INC_OP  */
    DEC_OP = 311,                  /* DEC_OP  */
    LE_OP = 312,                   /* LE_OP  */
    GE_OP = 313,                   /* GE_OP  */
    EQ_OP = 314,                   /* EQ_OP  */
    NE_OP = 315,                   /* NE_OP  */
    AND_OP = 316,                  /* AND_OP  */
    OR_OP = 317,                   /* OR_OP  */
    XOR_OP = 318,                  /* XOR_OP  */
    MUL_ASSIGN = 319,              /* MUL_ASSIGN  */
    DIV_ASSIGN = 320,              /* DIV_ASSIGN  */
    ADD_ASSIGN = 321,              /* ADD_ASSIGN  */
    MOD_ASSIGN = 322,              /* MOD_ASSIGN  */
    LEFT_ASSIGN = 323,             /* LEFT_ASSIGN  */
    RIGHT_ASSIGN = 324,            /* RIGHT_ASSIGN  */
    AND_ASSIGN = 325,              /* AND_ASSIGN  */
    XOR_ASSIGN = 326,              /* XOR_ASSIGN  */
    OR_ASSIGN = 327,               /* OR_ASSIGN  */
    SUB_ASSIGN = 328,              /* SUB_ASSIGN  */
    INVARIANT = 329,               /* INVARIANT  */
    PRECISE = 330,                 /* PRECISE  */
    LOWP = 331,                    /* LOWP  */
    MEDIUMP = 332,                 /* MEDIUMP  */
    HIGHP = 333,                   /* HIGHP  */
    SUPERP = 334,                  /* SUPERP  */
    PRECISION = 335,               /* PRECISION  */
    VERSION_TOK = 336,             /* VERSION_TOK  */
    EXTENSION = 337,               /* EXTENSION  */
    LINE = 338,                    /* LINE  */
    COLON = 339,                   /* COLON  */
    EOL = 340,                     /* EOL  */
    INTERFACE_TOK = 341,           /* INTERFACE_TOK  */
    OUTPUT = 342,                  /* OUTPUT  */
    PRAGMA_DEBUG_ON = 343,         /* PRAGMA_DEBUG_ON  */
    PRAGMA_DEBUG_OFF = 344,        /* PRAGMA_DEBUG_OFF  */
    PRAGMA_OPTIMIZE_ON = 345,      /* PRAGMA_OPTIMIZE_ON  */
    PRAGMA_OPTIMIZE_OFF = 346,     /* PRAGMA_OPTIMIZE_OFF  */
    PRAGMA_WARNING_ON = 347,       /* PRAGMA_WARNING_ON  */
    PRAGMA_WARNING_OFF = 348,      /* PRAGMA_WARNING_OFF  */
    PRAGMA_INVARIANT_ALL = 349,    /* PRAGMA_INVARIANT_ALL  */
    LAYOUT_TOK = 350,              /* LAYOUT_TOK  */
    DOT_TOK = 351,                 /* DOT_TOK  */
    ASM = 352,                     /* ASM  */
    CLASS = 353,                   /* CLASS  */
    UNION = 354,                   /* UNION  */
    ENUM = 355,                    /* ENUM  */
    TYPEDEF = 356,                 /* TYPEDEF  */
    TEMPLATE = 357,                /* TEMPLATE  */
    THIS = 358,                    /* THIS  */
    PACKED_TOK = 359,              /* PACKED_TOK  */
    GOTO = 360,                    /* GOTO  */
    INLINE_TOK = 361,              /* INLINE_TOK  */
    NOINLINE = 362,                /* NOINLINE  */
    PUBLIC_TOK = 363,              /* PUBLIC_TOK  */
    STATIC = 364,                  /* STATIC  */
    EXTERN = 365,                  /* EXTERN  */
    EXTERNAL = 366,                /* EXTERNAL  */
    LONG_TOK = 367,                /* LONG_TOK  */
    SHORT_TOK = 368,               /* SHORT_TOK  */
    HALF = 369,                    /* HALF  */
    FIXED_TOK = 370,               /* FIXED_TOK  */
    UNSIGNED = 371,                /* UNSIGNED  */
    INPUT_TOK = 372,               /* INPUT_TOK  */
    HVEC2 = 373,                   /* HVEC2  */
    HVEC3 = 374,                   /* HVEC3  */
    HVEC4 = 375,                   /* HVEC4  */
    FVEC2 = 376,                   /* FVEC2  */
    FVEC3 = 377,                   /* FVEC3  */
    FVEC4 = 378,                   /* FVEC4  */
    SAMPLER3DRECT = 379,           /* SAMPLER3DRECT  */
    SIZEOF = 380,                  /* SIZEOF  */
    CAST = 381,                    /* CAST  */
    NAMESPACE = 382,               /* NAMESPACE  */
    USING = 383,                   /* USING  */
    RESOURCE = 384,                /* RESOURCE  */
    PATCH = 385,                   /* PATCH  */
    SUBROUTINE = 386,              /* SUBROUTINE  */
    ERROR_TOK = 387,               /* ERROR_TOK  */
    COMMON = 388,                  /* COMMON  */
    PARTITION = 389,               /* PARTITION  */
    ACTIVE = 390,                  /* ACTIVE  */
    FILTER = 391,                  /* FILTER  */
    ROW_MAJOR = 392,               /* ROW_MAJOR  */
    THEN = 393                     /* THEN  */
  };
  typedef enum yytokentype yytoken_kind_t;
#endif

/* Value type.  */
#if ! defined YYSTYPE && ! defined YYSTYPE_IS_DECLARED
union YYSTYPE
{
#line 101 "../src/compiler/glsl/glsl_parser.yy"

   int n;
   int64_t n64;
   float real;
   double dreal;
   const char *identifier;

   struct ast_type_qualifier type_qualifier;

   ast_node *node;
   ast_type_specifier *type_specifier;
   ast_array_specifier *array_specifier;
   ast_fully_specified_type *fully_specified_type;
   ast_function *function;
   ast_parameter_declarator *parameter_declarator;
   ast_function_definition *function_definition;
   ast_compound_statement *compound_statement;
   ast_expression *expression;
   ast_declarator_list *declarator_list;
   ast_struct_specifier *struct_specifier;
   ast_declaration *declaration;
   ast_switch_body *switch_body;
   ast_case_label *case_label;
   ast_case_label_list *case_label_list;
   ast_case_statement *case_statement;
   ast_case_statement_list *case_statement_list;
   ast_interface_block *interface_block;
   ast_subroutine_list *subroutine_list;
   struct {
      ast_node *cond;
      ast_expression *rest;
   } for_rest_statement;

   struct {
      ast_node *then_statement;
      ast_node *else_statement;
   } selection_rest_statement;

   const glsl_type *type;

#line 243 "src/compiler/glsl/glsl_parser.h"

};
typedef union YYSTYPE YYSTYPE;
# define YYSTYPE_IS_TRIVIAL 1
# define YYSTYPE_IS_DECLARED 1
#endif

/* Location type.  */
#if ! defined YYLTYPE && ! defined YYLTYPE_IS_DECLARED
typedef struct YYLTYPE YYLTYPE;
struct YYLTYPE
{
  int first_line;
  int first_column;
  int last_line;
  int last_column;
};
# define YYLTYPE_IS_DECLARED 1
# define YYLTYPE_IS_TRIVIAL 1
#endif




int _mesa_glsl_parse (struct _mesa_glsl_parse_state *state);


#endif /* !YY__MESA_GLSL_SRC_COMPILER_GLSL_GLSL_PARSER_H_INCLUDED  */
