#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#include "string_utils.h"

const char* AllSeparators = " \t\n\r.,;()[]{}-<>+*/%&\\\"'^$=!:?";

char* gl4es_resize_if_needed(char* pBuffer, int *size, int addsize);

char* gl4es_inplace_replace(char* pBuffer, int* size, const char* S, const char* D)
{
    int lS = strlen(S), lD = strlen(D);
    pBuffer = gl4es_resize_if_needed(pBuffer, size, (lD-lS)*gl4es_count_string(pBuffer, S));
    char* p = pBuffer;
    while((p = strstr(p, S)))
    {
        // found an occurrence of S
        // check if good to replace, strchr also found '\0' :)
        if(strchr(AllSeparators, p[lS])!=NULL && (p==pBuffer || strchr(AllSeparators, p[-1])!=NULL)) {
            // move out rest of string
            memmove(p+lD, p+lS, strlen(p)-lS+1);
            // replace
            memcpy(p, D, strlen(D));
            // next
            p+=lD;
        } else p+=lS;
    }
    
    return pBuffer;
}

char* gl4es_inplace_insert(char* pBuffer, const char* S, char* master, int* size)
{
    char* m = gl4es_resize_if_needed(master, size, strlen(S));
    if(m!=master) {
        pBuffer += (m-master);
        master = m;
    }
    char* p = pBuffer;
    int lS = strlen(S), ll = strlen(pBuffer);
    memmove(p+lS, p, ll+1);
    memcpy(p, S, lS);

    return master;
}

char* gl4es_getline(char* pBuffer, int num)
{
    char *p = pBuffer;
    while(num-- && (p=strstr(p, "\n"))) p+=strlen("\n");
    return (p)?p:pBuffer;
}

int gl4es_countline(const char* pBuffer)
{
    int n=0;
    const char* p = pBuffer;
    while((p=strstr(p, "\n"))) {
        p+=strlen("\n");
        n++;
    }
    return n;
}

int gl4es_getline_for(const char* pBuffer, const char* S)
{
    int n=0;
    const char* p = pBuffer;
    const char* end = gl4es_find_string(pBuffer, S);
    if(!end)
        return 0;
    while((p=strstr(p, "\n"))) {
        p+=strlen("\n");
        n++;
        if(p>=end)
            return n;
    }
    return n;
}

int gl4es_count_string(const char* pBuffer, const char* S)
{
    const char* p = pBuffer;
    int lS = strlen(S);
    int n = 0;
    while((p = strstr(p, S)))
    {
        // found an occurrence of S
        // check if good to count, strchr also found '\0' :)
        if(strchr(AllSeparators, p[lS])!=NULL && (p==pBuffer || strchr(AllSeparators, p[-1])!=NULL))
            n++;
        p+=lS;
    }
    return n;
}

const char* gl4es_find_string(const char* pBuffer, const char* S)
{
    const char* p = pBuffer;
    int lS = strlen(S);
    while((p = strstr(p, S)))
    {
        // found an occurrence of S
        // check if good to count, strchr also found '\0' :)
        if(strchr(AllSeparators, p[lS])!=NULL && (p==pBuffer || strchr(AllSeparators, p[-1])!=NULL))
            return p;
        p+=lS;
    }
    return NULL;
}

char* gl4es_find_string_nc(char* pBuffer, const char* S)
{
    char* p = pBuffer;
    int lS = strlen(S);
    while((p = strstr(p, S)))
    {
        // found an occurrence of S
        // check if good to count, strchr also found '\0' :)
        if(strchr(AllSeparators, p[lS])!=NULL && (p==pBuffer || strchr(AllSeparators, p[-1])!=NULL))
            return p;
        p+=lS;
    }
    return NULL;
}

char* gl4es_resize_if_needed(char* pBuffer, int *size, int addsize) {
    char* p = pBuffer;
    int newsize = strlen(pBuffer)+addsize+1;
    if (newsize>*size) {
        newsize += 100;
        p = (char*)realloc(pBuffer, newsize);
        *size=newsize;
    }
    return p;
}

char* gl4es_append(char* pBuffer, int* size, const char* S) {
    char* p =pBuffer;
    p = gl4es_resize_if_needed(pBuffer, size, strlen(S));
    strcat(p, S);
    return p;
}

static int gl4es_is_blank(char c)  {
    switch(c) {
        case ' ':
        case '\t':
        case '\n':
        case '\r':
        case ':':
        case ',':
        case ';':
        case '/':
            return 1;
        default:
            return 0;
    }
}
char* gl4es_str_next(char *pBuffer, const char* S) {
    if(!pBuffer) return NULL;
    char *p = strstr(pBuffer, S);
    return (p)?p:(p+strlen(S));
}

char* gl4es_next_str(char* pBuffer) {
    if(!pBuffer) return NULL;
    while(gl4es_is_blank(*pBuffer))
        ++pBuffer;
    return pBuffer;
}

char* gl4es_prev_str(char* Str, char* pBuffer) {
    if(!pBuffer) return NULL;
    if(pBuffer == Str)
        return Str;
    // go to previous non blank
    do {
        --pBuffer;
    } while(gl4es_is_blank(*pBuffer) && (pBuffer!=Str));
    // go to blank
    while((pBuffer!=Str) && !gl4es_is_blank(*(pBuffer-1)))
        --pBuffer;
    return pBuffer;
}

char* gl4es_next_blank(char* pBuffer) {
    if(!pBuffer) return NULL;
    while(!gl4es_is_blank(*pBuffer))
        ++pBuffer;
    return pBuffer;
}

char* gl4es_next_line(char* pBuffer) {
    if(!pBuffer) return NULL;
    while(*pBuffer && *pBuffer!='\n')
        ++pBuffer;
    return pBuffer;
}

const char* gl4es_get_next_str(char* pBuffer) {
    static char buff[100] = {0};
    buff[0] = '\0';
    if(!pBuffer) return NULL;
    char* p1 = gl4es_next_str(pBuffer);
    if(!p1) return buff;
    char* p2 = gl4es_next_blank(p1);
    if(!p2) return buff;
    int i=0;
    while(p1!=p2 && i<99)
        buff[i++] = *(p1++);
    buff[i] = '\0';
    return buff;
}

int gl4es_countstring_simple(char* pBuffer, const char* S)
{
    char* p = pBuffer;
    int lS = strlen(S);
    int n = 0;
    while((p = strstr(p, S)))
    {
        // found an occurrence of S
        n++;
        p+=lS;
    }
    return n;
}

char* gl4es_inplace_replace_simple(char* pBuffer, int* size, const char* S, const char* D)
{
    int lS = strlen(S), lD = strlen(D);
    pBuffer = gl4es_resize_if_needed(pBuffer, size, (lD-lS)*gl4es_countstring_simple(pBuffer, S));
    char* p = pBuffer;
    while((p = strstr(p, S)))
    {
        // found an occurrence of S
        // move out rest of string
        memmove(p+lD, p+lS, strlen(p)-lS+1);
        // replace
        memcpy(p, D, strlen(D));
        // next
        p+=lD;
    }
    
    return pBuffer;
}
