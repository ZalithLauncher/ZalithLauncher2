#ifndef _GL4ES_ENVVARS_H_
#define _GL4ES_ENVVARS_H_
//----------------------------------------------------------------------------
const char* GetEnvVar(const char *name);
//----------------------------------------------------------------------------
int HasEnvVar(const char *name);
int ReturnEnvVarInt(const char *name);
int ReturnEnvVarIntDef(const char *name,int def);
//----------------------------------------------------------------------------
int IsEnvVarTrue(const char *name);
int IsEnvVarFalse(const char *name);
int IsEnvVarInt(const char *name,int i);
//----------------------------------------------------------------------------
int GetEnvVarInt(const char *name,int *i,int def);
int GetEnvVarBool(const char *name,int *b,int def);
int GetEnvVarFloat(const char *name,float *f,float def);
//----------------------------------------------------------------------------
int GetEnvVarFmt(const char *name,const char *fmt,...);
//----------------------------------------------------------------------------
#endif
