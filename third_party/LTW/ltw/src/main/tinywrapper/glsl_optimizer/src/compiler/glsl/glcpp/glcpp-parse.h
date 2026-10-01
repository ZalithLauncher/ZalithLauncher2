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

#ifndef YY_GLCPP_PARSER_SRC_COMPILER_GLSL_GLCPP_GLCPP_PARSE_H_INCLUDED
# define YY_GLCPP_PARSER_SRC_COMPILER_GLSL_GLCPP_GLCPP_PARSE_H_INCLUDED
/* Debug traces.  */
#ifndef YYDEBUG
# define YYDEBUG 1
#endif
#if YYDEBUG
extern int glcpp_parser_debug;
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
    DEFINED = 258,                 /* DEFINED  */
    ELIF_EXPANDED = 259,           /* ELIF_EXPANDED  */
    HASH_TOKEN = 260,              /* HASH_TOKEN  */
    DEFINE_TOKEN = 261,            /* DEFINE_TOKEN  */
    FUNC_IDENTIFIER = 262,         /* FUNC_IDENTIFIER  */
    OBJ_IDENTIFIER = 263,          /* OBJ_IDENTIFIER  */
    ELIF = 264,                    /* ELIF  */
    ELSE = 265,                    /* ELSE  */
    ENDIF = 266,                   /* ENDIF  */
    ERROR_TOKEN = 267,             /* ERROR_TOKEN  */
    IF = 268,                      /* IF  */
    IFDEF = 269,                   /* IFDEF  */
    IFNDEF = 270,                  /* IFNDEF  */
    LINE = 271,                    /* LINE  */
    PRAGMA = 272,                  /* PRAGMA  */
    UNDEF = 273,                   /* UNDEF  */
    VERSION_TOKEN = 274,           /* VERSION_TOKEN  */
    GARBAGE = 275,                 /* GARBAGE  */
    IDENTIFIER = 276,              /* IDENTIFIER  */
    IF_EXPANDED = 277,             /* IF_EXPANDED  */
    INTEGER = 278,                 /* INTEGER  */
    INTEGER_STRING = 279,          /* INTEGER_STRING  */
    LINE_EXPANDED = 280,           /* LINE_EXPANDED  */
    NEWLINE = 281,                 /* NEWLINE  */
    OTHER = 282,                   /* OTHER  */
    PLACEHOLDER = 283,             /* PLACEHOLDER  */
    SPACE = 284,                   /* SPACE  */
    PLUS_PLUS = 285,               /* PLUS_PLUS  */
    MINUS_MINUS = 286,             /* MINUS_MINUS  */
    PATH = 287,                    /* PATH  */
    INCLUDE = 288,                 /* INCLUDE  */
    PASTE = 289,                   /* PASTE  */
    OR = 290,                      /* OR  */
    AND = 291,                     /* AND  */
    EQUAL = 292,                   /* EQUAL  */
    NOT_EQUAL = 293,               /* NOT_EQUAL  */
    LESS_OR_EQUAL = 294,           /* LESS_OR_EQUAL  */
    GREATER_OR_EQUAL = 295,        /* GREATER_OR_EQUAL  */
    LEFT_SHIFT = 296,              /* LEFT_SHIFT  */
    RIGHT_SHIFT = 297,             /* RIGHT_SHIFT  */
    UNARY = 298                    /* UNARY  */
  };
  typedef enum yytokentype yytoken_kind_t;
#endif

/* Value type.  */

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




int glcpp_parser_parse (glcpp_parser_t *parser);


#endif /* !YY_GLCPP_PARSER_SRC_COMPILER_GLSL_GLCPP_GLCPP_PARSE_H_INCLUDED  */
