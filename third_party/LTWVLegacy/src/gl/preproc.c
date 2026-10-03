#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <locale.h>
#include "khash.h"

#include "preproc.h"
#include "string_utils.h"

//#define DEBUG
#ifdef DEBUG
#define DBG(a) a
#pragma GCC optimize 0
#else
#define DBG(a)
#endif

typedef enum _eTokenType {
    TK_NULL = 0,
    TK_SPACE,
    TK_SHARP,
    TK_NEWLINE,
    TK_INT,
    TK_FLOAT,
    TK_PLUS,
    TK_MINUS,
    TK_EQUAL,
    TK_SLASH,
    TK_DOUBLESLASH, //10
    TK_MULTIPLY,
    TK_BACKSLASH,
    TK_OPENBRACE,
    TK_CLOSEBRACE,
    TK_OPENCURLY,
    TK_CLOSECURLY,
    TK_OPENBRAKET,
    TK_CLOSEBRAKET,
    TK_OPENCOMMENT,
    TK_CLOSECOMMENT,    //20
    TK_COLUMN,
    TK_SEMICOLUMN,
    TK_COMMA,
    TK_DOT,
    TK_AMP,
    TK_POW,
    TK_PIPE,
    TK_EXCLAM,
    TK_POINT,   //30
    TK_GREATER,
    TK_LESS,
    TK_DOUBLEEQUAL,
    TK_TILDE,
    TK_TEXT,
    TK_TEXTCOMMENT
} eTokenType;

#define MAXSTR 500

typedef struct _uToken {
    eTokenType type;
    char    str[MAXSTR];
    int     integer;
    float   real;
} uToken;

eTokenType NextTokenComment(char **p, uToken *tok) {
    tok->type = TK_NULL;
    tok->str[0] = 0;
    if(!**p) return tok->type;

    char c = **p;
    (*p)++;
    char nextc = **p;
    int nb = 0;
    int isfloat = 0;
    int isneg = 1;  // multiply by it...
    float fnb = 0.f;
    int cnt = 0;
    float fcnt = 0.f;

    tok->str[0]=c; tok->str[1]=0;

    switch (c) {
        case 10:
            if(nextc==13)
                (*p)++;
            strcpy(tok->str, "\n");
            tok->type = TK_NEWLINE;
            break;
        case 13:
            if(nextc==10)
                (*p)++;
            strcpy(tok->str, "\n");
            tok->type = TK_NEWLINE;
            break;
        case ' ':
        case '\t':
            while(nextc==' ' || nextc=='\t') { int l=strlen(tok->str); tok->str[l]=nextc; tok->str[l+1]=0; (*p)++; nextc=**p; }
            tok->type = TK_SPACE;
            break;
        case '/':
            if(nextc=='/') {
                (*p)++;
                tok->type=TK_DOUBLESLASH;
                strcpy(tok->str, "//");
            } else if(nextc=='*') {
                (*p)++;
                tok->type=TK_OPENCOMMENT;
                strcpy(tok->str, "/*");
            } else {
                tok->type=TK_SLASH;
            }
            break;
        case '*':
            if(nextc=='/') {
                (*p)++;
                tok->type=TK_CLOSECOMMENT;
                strcpy(tok->str, "*/");
            } else {
                tok->type=TK_MULTIPLY;
            }
            break;
        default:
            // all other are plain Ids...
            cnt=1;
            tok->type=TK_TEXTCOMMENT;
            while(cnt!=(MAXSTR-1) && (nextc=='_' || (nextc>='0' && nextc<='9') || (nextc>='A' && nextc<='Z') || (nextc>='a' && nextc<='z'))) {
                tok->str[cnt]=nextc;
                (*p)++; nextc=**p;
                ++cnt;
            }
            tok->str[cnt]=0;
    }

    return tok->type;
}

eTokenType NextToken(char **p, uToken *tok) {
    tok->type = TK_NULL;
    tok->str[0] = 0;
    if(!**p) return tok->type;

    char c = **p;
    (*p)++;
    char nextc = **p;
    int nb = 0;
    int isfloat = 0;
    int isneg = 1;  // multiply by it...
    float fnb = 0.f;
    int cnt = 0;
    float fcnt = 0.f;

    tok->str[0]=c; tok->str[1]=0;

    switch (c) {
        case 10:
            if(nextc==13)
                (*p)++;
            tok->type = TK_NEWLINE;
            strcpy(tok->str, "\n");
            break;
        case 13:
            if(nextc==10)
                (*p)++;
            tok->type = TK_NEWLINE;
            strcpy(tok->str, "\n");
            break;
        case ' ':
        case '\t':
            while(nextc==' ' || nextc=='\t') { int l=strlen(tok->str); tok->str[l]=nextc; tok->str[l+1]=0; (*p)++; nextc=**p; }
            tok->type = TK_SPACE;
            break;
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            nb=c-'0';
            // we don't know if it's an int or a float at first...
            if(nextc=='x' && nb==0) {
                // hex number, so int...
                (*p)++; nextc=**p;
                while((nextc>='0' && nextc<='9') || (nextc>='a' && nextc<='f') || (nextc>='A' && nextc<='F')) { 
                    nb=nb*16;
                    if(nextc>='0' && nextc<='9')
                        nb+=nextc-'0';
                    else if(nextc>='a' && nextc<='f')
                        nb+=nextc-'a'+10;
                    else if(nextc>='A' && nextc<='F')
                        nb+=nextc-'A'+10;
                    (*p)++; nextc=**p;
                }
                tok->type = TK_INT;
                tok->integer = nb;
                sprintf(tok->str, "0x%x", nb);
            } else {
                while(nextc>='0' && nextc<='9') { nb=nb*10+nextc-'0'; (*p)++; nextc=**p;}
                if(nextc=='.' || nextc=='f' || nextc=='e') {
                    isfloat = 1;
                    fnb=nb;
                    if(nextc=='.') {
                        (*p)++; nextc=**p;
                        fcnt = 0.1f;
                        while(nextc>='0' && nextc<='9') { 
                            fnb+=(nextc-'0')*fcnt;
                            fcnt/=10.f;
                            (*p)++; nextc=**p;
                        }
                    }
                    if(nextc=='e') {
                        // exponent
                        (*p)++; nextc=**p;
                        nb=0;
                        int expsign = 1;
                        if(nextc=='-' || nextc=='+') {
                            if(nextc=='-') expsign = -1;
                            (*p)++; nextc=**p;
                        }
                        while(nextc>='0' && nextc<='9') { nb=nb*10+nextc-'0'; (*p)++; nextc=**p;}
                        fnb *= powf(10, nb*expsign); // exp10f is a GNU extension
                    }
                    if(nextc=='f') {
                        (*p)++; nextc=**p;
                    }
                    fnb*=isneg;
                    tok->type = TK_FLOAT;
                    tok->real = fnb;
                    sprintf(tok->str, "%#g", fnb);
                } else {
                    tok->type = TK_INT;
                    tok->integer = nb;
                    sprintf(tok->str, "%d", nb);
                }
            }
            break;
        case '/':
            if(nextc=='/') {
                (*p)++;
                tok->type=TK_DOUBLESLASH;
                strcpy(tok->str, "//");
            } else if(nextc=='*') {
                (*p)++;
                tok->type=TK_OPENCOMMENT;
                strcpy(tok->str, "/*");
            } else {
                tok->type=TK_SLASH;
            }
            break;
        case '*':
            if(nextc=='/') {
                (*p)++;
                tok->type=TK_CLOSECOMMENT;
                strcpy(tok->str, "*/");
            } else {
                tok->type=TK_MULTIPLY;
            }
            break;
        case '=': tok->type=TK_EQUAL; break;
        case '~': tok->type=TK_TILDE; break;
        case '#': tok->type=TK_SHARP; break;
        case '(': tok->type=TK_OPENBRACE; break;
        case ')': tok->type=TK_CLOSEBRACE; break;
        case '{': tok->type=TK_OPENCURLY; break;
        case '}': tok->type=TK_CLOSECURLY; break;
        case '[': tok->type=TK_OPENBRAKET; break;
        case ']': tok->type=TK_CLOSEBRAKET; break;
        case '&': tok->type=TK_AMP; break;
        case '^': tok->type=TK_POW; break;
        case '|': tok->type=TK_PIPE; break;
        case '\\': tok->type=TK_BACKSLASH; break;
        case '<': tok->type=TK_LESS; break;
        case '>': tok->type=TK_GREATER; break;
        case '+': tok->type=TK_PLUS; break;
        case '-': tok->type=TK_MINUS; break;
        case ':': tok->type=TK_COLUMN; break;
        case ';': tok->type=TK_SEMICOLUMN; break;
        case ',': tok->type=TK_COMMA; break;
        case '.': tok->type=TK_DOT; break;
        case '!': tok->type=TK_EXCLAM; break;
        // todo: char and string?
        default:
            // all other are plain Ids...
            cnt=1;
            tok->type=TK_TEXT;
            while(cnt!=(MAXSTR-1) && (nextc=='_' || (nextc>='0' && nextc<='9') || (nextc>='A' && nextc<='Z') || (nextc>='a' && nextc<='z'))) {
                tok->str[cnt]=nextc;
                (*p)++; nextc=**p;
                ++cnt;
            }
            tok->str[cnt]=0;
    }

    return tok->type;
}

eTokenType GetToken(char **p, uToken *tok, int incomment) {
    eTokenType ret;
    if (incomment)
        ret = NextTokenComment(p, tok);
    else
        ret = NextToken(p, tok);
    return ret;
}

typedef struct {
    int cap;
    int sz;
    int *ifs;
} stackif_t;

static void push_if(stackif_t *st, int v) {
    if(st->sz == st->cap) {
        st->cap += 16;
        st->ifs = (int*)realloc(st->ifs, sizeof(int)*st->cap);
    }
    st->ifs[st->sz++] = v;
}

static int pop_if(stackif_t *st) {
    if(st->sz)
        return st->ifs[--st->sz];
    return -1;
}

static int top_if(stackif_t *st) {
    if(st->sz)
        return st->ifs[st->sz-1];
    return -1;
}
static void not_top_if(stackif_t *st) {
    if(st->sz && st->ifs[st->sz-1]!=-1)
        st->ifs[st->sz-1] = 1 - st->ifs[st->sz-1];
}

static int result_if(stackif_t *st) {
    for (int i=0; i<st->sz; ++i) {
        if(st->ifs[i] == 1) return 1;
        else if(st->ifs[i] == -1) return -1;
    }
    return 0;
}

KHASH_MAP_INIT_STR(define, int);
KHASH_MAP_INIT_STR(alldefine, char*);

char* preproc(const char* code, int keepcomments, int gl_es, extensions_t* exts, char** versionString) {
    DBG(printf("Preproc on: =========\n%s\n=================\n", code);)

    uToken tok;
    char* p = (char*)code;
    char* oldp = NULL;
    int cap=1000;
    char* ncode = (char*)malloc(1000);
    ncode[0]=0;
    int sz=1;
    int status=0;
    int write=1;
    int incomment=0;
    int indefined=0;
    int newline=0;
    int gettok=0;
    int notok = 0;
    char extname[50];
    khint_t k;
    int ret;
    char *defname;   //used for #define or #if
    int  defval;
    stackif_t stackif = {0};
    int nowrite_ifs = 0;
    int current_if = 0;
    int need_pop = 0;

    char* old_locale = setlocale(LC_ALL, "C");

    khash_t(alldefine) *alldefines = kh_init(alldefine);  // will conatin all defines, even the one without int inside

    khash_t(define) *defines = kh_init(define);
    if(gl_es) {
        k = kh_put(define, defines, "GL_ES", &ret);
        kh_value(defines, k) = 0;
    }
    push_if(&stackif, 0);   // default to OK
    GetToken(&p, &tok, incomment);
    while(tok.type!=TK_NULL) {
        // pop #if / #endif status
        if(need_pop) {
            pop_if(&stackif);
            nowrite_ifs = result_if(&stackif);
            need_pop = 0;

        }
        // pre get token switch
        switch(status) {
            case 110:   // line comment done...
            case 210:   // block comment done
                if(!write && newline) {
                    gettok=0;
                    tok.type=TK_NEWLINE;
                    strcpy(tok.str, "\n");
                }
                write = 1;
                status = (status==210)?1:0;
                incomment=0;
                newline=0;
            break;
        }
        // get token (if needed)
        if (gettok) GetToken(&p, &tok, incomment);
        gettok=1;
        // post get token switch
        if(tok.type!=TK_NULL) {
            switch(status) {
                case 0: // regular...
                case 1:
                    if(tok.type==TK_DOUBLESLASH) {
                        status = 100; // line comment
                        incomment = 1;
                        newline = 1;
                        if(!keepcomments) write=0;
                    } else if(tok.type==TK_OPENCOMMENT) {
                        status = 200; // multi-line comment
                        incomment = 1;
                        if(!keepcomments) write=0;
                    } else if(tok.type==TK_SHARP && !incomment && status==0) {
                        oldp = p-1; // lets buffer the line
                        status = 300;
                    } else if(tok.type==TK_NEWLINE)
                        status = 0;
                    else if(tok.type!=TK_SPACE)
                        status = 1; // everything else but space set status to 1...
                    break;

                // line comment...
                case 100:
                    if(tok.type==TK_BACKSLASH) {
                        status = 120;   // is it backslash+endline for multiline?
                    } else if(tok.type==TK_NEWLINE) {
                        status = 110;
                    }
                    break;
                case 120:
                    status = 100;   // continue comment, what ever follow (NewLine or anything else)
                    break;

                // block comment...
                case 200:
                    if(tok.type==TK_NEWLINE)
                        newline=1;
                    if(tok.type==TK_CLOSECOMMENT) {
                        status=210;
                    }
                    break;
                
                // # (of ifdef or extension)
                case 300:
                    if(tok.type!=TK_SPACE)
                    if(tok.type==TK_TEXT) {
                        if(!strcmp(tok.str, "ifdef"))
                            status=310;
                        else if(!strcmp(tok.str, "ifndef"))
                            status=320;
                        else if(!strcmp(tok.str, "if")) {
                            // #if defined(GL_ES) not supported for now
                            push_if(&stackif, -1);
                            if(nowrite_ifs==1) {
                                status = 398; notok = 1;
                            } else {
                                status = 390;
                                {
                                    int l = p - oldp;
                                    memcpy(tok.str, oldp, l);
                                    tok.str[l]='\0';
                                    oldp = 0;
                                }
                            }
                            nowrite_ifs = result_if(&stackif);
                        } else if(!strcmp(tok.str, "else")) {
                            status = 399;
                            // ifs handling
                            {
                                not_top_if(&stackif);
                                int v = result_if(&stackif);
                                if(v!=-1) {
                                    notok = 1;
                                    status = 398;
                                }
                                nowrite_ifs = v;
                            }
                        } else if(!strcmp(tok.str, "endif")) {
                            status = 399;
                            {
                                need_pop = 1;
                                if(nowrite_ifs!=-1) {
                                    notok = 1;
                                    status = 398;
                                }
                            }
                        } else if(!strcmp(tok.str, "extension")) {
                            status = 410;
                        } else if(!strcmp(tok.str, "pragma")) {
                            status = 510;
                        } else if(!strcmp(tok.str, "define")) {
                            status = 610;
                        } else if(!strcmp(tok.str, "version")) {
                            status = 810;
                            if(!*versionString)
                                *versionString = (char*)calloc(1, 51);
                        } else status=399;
                    } else status = 399;  // meh?
                    break;

                // ifdef
                case 310:
                    if(tok.type==TK_SPACE)
                        status = 310;
                    else if(tok.type==TK_TEXT) {
                        int v = 1;
                        if(gl_es && (strcmp(tok.str, "GL_ES")==0))
                            v = 1;
                        else if(kh_get(alldefine, alldefines, tok.str)!=kh_end(alldefines)) {
                            v = 0;
                        } else if (strncmp(tok.str, "GL_", 3)==0)
                            v = -1;
                        push_if(&stackif, v);
                        nowrite_ifs = result_if(&stackif);
                        if(nowrite_ifs!=-1) {
                            status = 398; notok = 1;
                        } else
                            status = 399;
                    } else {push_if(&stackif, -1); nowrite_ifs = result_if(&stackif);status = 399;}
                    break;
                
                // ifndef
                case 320:
                    if(tok.type==TK_SPACE)
                        status = 320;
                    else if(tok.type==TK_TEXT) {
                        int v = 0;
                        if(gl_es && strcmp(tok.str, "GL_ES")==0)
                            v = 0;
                        else if(kh_get(alldefine, alldefines, tok.str)!=kh_end(alldefines)) {
                            v = 1;
                        } else if (strncmp(tok.str, "GL_", 3)==0)
                            v = -1;
                        push_if(&stackif, v);
                        nowrite_ifs = result_if(&stackif);
                        if(nowrite_ifs!=-1) {
                            status = 398; notok = 1;
                        } else
                            status = 399;
                    } else {push_if(&stackif, -1); nowrite_ifs = result_if(&stackif); status = 399;}
                    break;

                // #if ...
                case 390:
                    if (tok.type == TK_NEWLINE) {
                        status = 0;
                    } else if (tok.type == TK_TEXT) {
                        if(!strcmp(tok.str, "defined"))
                            status = 710;
                    }
                    break;

                // end of #ifdef GL_ES and variant..
                case 398:
                    if(tok.type==TK_NEWLINE) {
                        oldp = NULL;
                        status = 0;
                    }
                    break;

                // fallback for #ifdef GL_ES, write the line back...
                case 399:
                    if(oldp)
                    {
                        int l = p - oldp;
                        memcpy(tok.str, oldp, l);
                        tok.str[l]='\0';
                        oldp = NULL;
                    }
                    status = (tok.type==TK_NEWLINE)?0:1;
                    break;
                // #extension
                case 410:
                    if(tok.type==TK_SPACE) {
                        // nothing...
                    } else if(tok.type==TK_TEXT && strlen(tok.str)<50) {
                        strcpy(extname, tok.str);
                        status = 420;
                    } else {
                        status = 399; // fallback, syntax error...
                    }
                    break;
                // after the name and before the ':' of #extension
                case 420:
                    if(tok.type==TK_SPACE) {
                        // nothing...
                    } else if (tok.type==TK_COLUMN) {
                        status=430;
                    } else {
                        status = 399; // fallback, syntax error...
                    }
                    break;
                // after the ':' of #extension
                case 430:
                    if(tok.type==TK_SPACE) {
                        // nothing...
                    } else if(tok.type==TK_TEXT) {
                        int state = -1;
                        if(!strcmp(tok.str, "disable"))
                            state = 0;
                        else if(!strcmp(tok.str, "warn"))
                            state = 1;
                        else if(!strcmp(tok.str, "enable"))
                            state = 1;
                        else if(!strcmp(tok.str, "require"))
                            state = 1;
                        if(state!=-1) {
                            if(exts->size==exts->cap) {
                                exts->cap += 4;
                                exts->ext = (extension_t*)realloc(exts->ext, sizeof(extension_t)*exts->cap);
                            }
                            strcpy(exts->ext[exts->size].name, extname);
                            exts->ext[exts->size].state = state;
                            ++exts->size;
                            status = 398;   // all done
                        } else
                            status = 399; // error, unknown keyword
                    } else {
                        status = 399; // fallback, syntax error...
                    }
                    break;
                // #pragma
                case 510:
                    if(tok.type==TK_SPACE) {
                        // nothing...
                    } else if(tok.type==TK_TEXT && strlen(tok.str)<50) {
                        if(strcmp(tok.str, "message")==0)
                            status = 398; //pragma message are removed
                        else if(strcmp(tok.str, "parameter")==0)
                            status = 398; //pragma message are removed
                        else
                            status = 399;   // other pragma as left as-is
                    } else {
                        status = 399; // fallback, syntax error...
                    }
                    break;
                // #define
                case 610:
                    if(tok.type==TK_SPACE) {
                        // nothing...
                    } else if(tok.type==TK_TEXT && strlen(tok.str)<50) {
                        defname = strdup(tok.str);
                        k = kh_put(alldefine, alldefines, defname, &ret);
                        kh_value(alldefines, k) = defname;
                        status = 620; // and now get the value
                    } else {
                        status = 399; // fallback, define name too long...
                    }
                    break;
                case 620:
                    if(tok.type==TK_SPACE) {
                        // nothing...
                    } else if(tok.type==TK_INT) {
                        defval = tok.integer;
                        status = 630; // check if end of line (so it's a simple define)
                    } else if(tok.type==TK_NEWLINE) {
                        {
                            int l = p - oldp;
                            memcpy(tok.str, oldp, l);
                            tok.str[l]='\0';
                            oldp = 0;
                        }
                        status = 0;
                    } else {
                        status = 399; // fallback
                    }
                    break;
                case 630:
                    if(tok.type==TK_SPACE) {
                        // nothing...
                    } else if(tok.type==TK_NEWLINE) {
                        k = kh_put(define, defines, defname, &ret);
                        kh_value(defines, k) = defval;
                        {
                            int l = p - oldp;
                            memcpy(tok.str, oldp, l);
                            tok.str[l]='\0';
                            oldp = 0;
                        }
                        status = 0; // ok, define added to collection, left the line as-is anyway
                    } else {
                        status = 399; // fallback
                    }
                    break;
                // #defined
                case 710:
                    if(tok.type==TK_SPACE) {
                        // nothing...
                    } else if(tok.type==TK_OPENBRACE) {
                        status = 720; // and now get the value
                        ++indefined;
                    } else if(tok.type==TK_NEWLINE) {
                        if(!indefined) status = 0; // ok... no handling of #defined for now, so just write through the line
                    }/* else {
                        status = 399; // No fallback...
                    }*/
                    break;
                case 720:
                    if(tok.type==TK_SPACE || tok.type==TK_TEXT) {
                       // nothing...
                    } else if (tok.type==TK_CLOSEBRACE) {
                        --indefined;
                        status = 710;
                    } else {
                        indefined = 0;
                        status = 399;
                    }
                    break;
                // #version
                case 810:
                    if(tok.type==TK_SPACE) {
                        // nothing...
                    } else if(tok.type==TK_TEXT) {
                        strncat(*versionString, tok.str, 50);
                        status = 820;
                    } else if(tok.type==TK_INT) {
                        char buff[20] = {0};
                        sprintf(buff, "%d", tok.integer);
                        strncat(*versionString, buff, 50);
                        status = 820;
                    } else if(tok.type==TK_FLOAT) {
                        char buff[20] = {0};
                        sprintf(buff, "%g", tok.real);
                        strncat(*versionString, buff, 50);
                        status = 820;
                    } else {
                        status = 399; // fallback, syntax error...
                    }
                    break;
                case 820:
                    if(tok.type==TK_SPACE) {
                        strncat(*versionString, " ", 50);
                        status = 830;
                    } else if(tok.type==TK_TEXT) {
                        strncat(*versionString, tok.str, 50);
                    } else if(tok.type==TK_INT) {
                        char buff[20] = {0};
                        sprintf(buff, "%d", tok.integer);
                        strncat(*versionString, buff, 50);
                    } else if(tok.type==TK_FLOAT) {
                        char buff[20] = {0};
                        sprintf(buff, "%g", tok.real);
                        strncat(*versionString, buff, 50);
                    } else if(tok.type==TK_NEWLINE) {
                        oldp = NULL;
                        status = 0;
                    } else {
                        status = 399; // fallback, syntax error...
                    }
                    break;
                case 830:
                    if(tok.type==TK_SPACE) {
                        status = 830;
                    } else if(tok.type==TK_TEXT) {
                        strncat(*versionString, tok.str, 50);
                        status = 820;
                    } else if(tok.type==TK_INT) {
                        char buff[20] = {0};
                        sprintf(buff, "%d", tok.integer);
                        strncat(*versionString, buff, 50);
                        status = 820;
                    } else if(tok.type==TK_FLOAT) {
                        char buff[20] = {0};
                        sprintf(buff, "%g", tok.real);
                        strncat(*versionString, buff, 50);
                        status = 820;
                    } else if(tok.type==TK_NEWLINE) {
                        oldp = NULL;
                        status = 0;
                    } else {
                        status = 399; // fallback, syntax error...
                    }
                    break;
            }
            if(notok)
                notok=0;
            else
                if(write && !oldp && nowrite_ifs!=1) {
                    if(!incomment && !indefined && tok.type == TK_TEXT) {
                        k = kh_get(define, defines, tok.str);
                        if(k!=kh_end(defines)) {
                            int v = kh_val(defines, k);
                            sprintf(tok.str, "%d", v); // overide define with defined value
                        }
                    }
                    int l = strlen(tok.str);
                    if(sz+l>=cap) {
                        cap+=2000;
                        ncode = (char*)realloc(ncode, cap);
                    }
                    strcat(ncode, tok.str);
                    sz+=l;
                }
        }
    }

    DBG(printf("New code is: ------------\n%s\n------------------\n", ncode);)
    kh_destroy(define, defines);
    kh_foreach_value(alldefines, defname,
            free(defname);
        )
    kh_destroy(alldefine, alldefines);
    if(stackif.ifs)
        free(stackif.ifs);

    setlocale(LC_ALL, old_locale);
    return ncode;
}
