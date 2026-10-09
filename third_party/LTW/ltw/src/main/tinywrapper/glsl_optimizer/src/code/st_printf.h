#ifndef __ST_PRINTF__H__
#define __ST_PRINTF__H__

#include <stdarg.h>
#include <string>

#include "../util/macros.h"
#include "../util/compat_layer.h"


class st
{
public:
	st() {}

	static void stprintf(std::string& buffer, const char *fmt, ...)
	{
		if (fmt)
		{
			va_list args;
			va_start(args, fmt);

			size_t size = vscprintf(fmt, args);
			char *buf = new char[size + 1];
			size_t nsize = vsnprintf(buf, size, fmt, args);
			buffer += buf;
			delete[] buf;

			va_end(args);
		}
	}
};

/* Original code come from https://github.com/aras-p/glsl-optimizer

GLSL Optimizer is licensed according to the terms of the MIT license :

Copyright(C) 1999 - 2007  Brian Paul   All Rights Reserved.
Copyright(C) 2010 - 2013  Unity Technologies All Rights Reserved.

Permission is hereby granted, free of charge, to any person obtaining a
copy of this software and associated documentation files(the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following conditions :

The above copyright notice and this permission notice shall be included
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL
BRIAN PAUL BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

class sbuffer
{
public:
	sbuffer()
	{
		m_Capacity = 512;
		m_Ptr = (char*)malloc(m_Capacity);
		m_Size = 0;
		m_Ptr[0] = 0;
        m_Ownership_Lost = false;
	}

	~sbuffer()
	{
        if(!m_Ownership_Lost)
		    free(m_Ptr);
	}

	bool empty() const { return m_Size == 0; }

	const char* c_str() const { return m_Ptr; }

    /**
     * Dangerous, get the string and declare the ownership lost.
     * Meaning the lifecycle of the char * goes beyond what the class normally allows.
     * @return A C style string
     */
    char * c_str_take_ownership() {
        m_Ownership_Lost = true;
		return m_Ptr;
    }

	void append(const char *fmt, ...) PRINTFLIKE(2, 3)
	{
		va_list args;
		va_start(args, fmt);
		vasprintf_append(fmt, args);
		va_end(args);
	}

	void vasprintf_append(const char *fmt, va_list args)
	{
		assert(m_Ptr != NULL);
		vasprintf_rewrite_tail(&m_Size, fmt, args);
	}

	void vasprintf_rewrite_tail(size_t *start, const char *fmt, va_list args)
	{
		assert(m_Ptr != NULL);

		size_t new_length = vscprintf(fmt, args);
		size_t needed_length = m_Size + new_length + 1;

		if (m_Capacity < needed_length)
		{
			m_Capacity = MAX2(m_Capacity + m_Capacity / 2, needed_length);
            m_Ptr = (char*)realloc(m_Ptr, m_Capacity);
		}

		vsnprintf(m_Ptr + m_Size, new_length + 1, fmt, args);
		m_Size += new_length;
		assert(m_Capacity >= m_Size);
	}

private:
	char* m_Ptr;
	size_t m_Size;
	size_t m_Capacity;
    bool m_Ownership_Lost;
};

#endif // __ST_PRINTF__H__