/*
 * Pixel Dungeon 3D
 * Copyright (C) 2016-2018 Alex Fomins
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package android.opengl;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class GLES20 {

    public static final int GL_FLOAT = 0;
    public static final int GL_COLOR_ATTACHMENT0 = 0;
    public static final int GL_DEPTH_ATTACHMENT = 0;
    public static final int GL_STENCIL_ATTACHMENT = 0;
    public static final int GL_FRAMEBUFFER = 0;
    public static final int GL_TEXTURE_2D = 0;
    public static final int GL_RENDERBUFFER = 0;
    public static final int GL_FRAMEBUFFER_COMPLETE = 0;
    public static final int GL_LINK_STATUS = 0;
    public static final int GL_FALSE = 0;
    public static final int GL_RGBA = 0;
    public static final int GL_DEPTH_COMPONENT16 = 0;
    public static final int GL_STENCIL_INDEX8 = 0;
    public static final int GL_VERTEX_SHADER = 0;
    public static final int GL_FRAGMENT_SHADER = 0;
    public static final int GL_COMPILE_STATUS = 0;
    public static final int GL_NEAREST = 0;
    public static final int GL_LINEAR = 0;
    public static final int GL_REPEAT = 0;
    public static final int GL_MIRRORED_REPEAT = 0;
    public static final int GL_CLAMP_TO_EDGE = 0;
    public static final int GL_TEXTURE0 = 0;
    public static final int GL_TEXTURE_MIN_FILTER = 0;
    public static final int GL_TEXTURE_MAG_FILTER = 0;
    public static final int GL_TEXTURE_WRAP_S = 0;
    public static final int GL_TEXTURE_WRAP_T = 0;
    public static final int GL_UNSIGNED_BYTE = 0;
    public static final int GL_UNPACK_ALIGNMENT = 0;
    public static final int GL_ALPHA = 0;
    public static final int GL_COLOR_BUFFER_BIT = 0;
    public static final int GL_TRIANGLES = 0;
    public static final int GL_UNSIGNED_SHORT = 0;

    public static void glEnableVertexAttribArray(int location) {
        // TODO Auto-generated method stub
        
    }

    public static void glDisableVertexAttribArray(int location) {
        // TODO Auto-generated method stub
        
    }

    public static void glVertexAttribPointer(int location, int size, int glFloat, boolean b, int i, FloatBuffer ptr) {
        // TODO Auto-generated method stub
        
    }

    public static void glGenBuffers(int i, int[] buffers, int j) {
        // TODO Auto-generated method stub
        
    }

    public static void glBindFramebuffer(int glFramebuffer, int id) {
        // TODO Auto-generated method stub
        
    }

    public static void glDeleteFramebuffers(int i, int[] buffers, int j) {
        // TODO Auto-generated method stub
        
    }

    public static void glFramebufferTexture2D(int glFramebuffer, int point, int glTexture2d, int id, int i) {
        // TODO Auto-generated method stub
        
    }

    public static void glFramebufferRenderbuffer(int glRenderbuffer, int point, int glTexture2d, int id) {
        // TODO Auto-generated method stub
        
    }

    public static int glCheckFramebufferStatus(int glFramebuffer) {
        // TODO Auto-generated method stub
        return 0;
    }

    public static int glCreateProgram() {
        // TODO Auto-generated method stub
        return 0;
    }

    public static void glAttachShader(int handle, int handle2) {
        // TODO Auto-generated method stub
        
    }

    public static void glLinkProgram(int handle) {
        // TODO Auto-generated method stub
        
    }

    public static void glGetProgramiv(int handle, int glLinkStatus, int[] status, int i) {
        // TODO Auto-generated method stub
        
    }

    public static String glGetProgramInfoLog(int handle) {
        // TODO Auto-generated method stub
        return null;
    }

    public static int glGetAttribLocation(int handle, String name) {
        // TODO Auto-generated method stub
        return 0;
    }

    public static int glGetUniformLocation(int handle, String name) {
        // TODO Auto-generated method stub
        return 0;
    }

    public static void glUseProgram(int handle) {
        // TODO Auto-generated method stub
        
    }

    public static void glDeleteProgram(int handle) {
        // TODO Auto-generated method stub
        
    }

    public static void glGenRenderbuffers(int i, int[] buffers, int j) {
        // TODO Auto-generated method stub
        
    }

    public static void glBindRenderbuffer(int glRenderbuffer, int id) {
        // TODO Auto-generated method stub
        
    }

    public static void glDeleteRenderbuffers(int i, int[] buffers, int j) {
        // TODO Auto-generated method stub
        
    }

    public static void glRenderbufferStorage(int glRenderbuffer, int format, int width, int height) {
        // TODO Auto-generated method stub
        
    }

    public static int glCreateShader(int type) {
        // TODO Auto-generated method stub
        return 0;
    }

    public static void glShaderSource(int handle, String src) {
        // TODO Auto-generated method stub
        
    }

    public static void glCompileShader(int handle) {
        // TODO Auto-generated method stub
        
    }

    public static void glGetShaderiv(int handle, int glCompileStatus, int[] status, int i) {
        // TODO Auto-generated method stub
        
    }

    public static String glGetShaderInfoLog(int handle) {
        // TODO Auto-generated method stub
        return null;
    }

    public static void glDeleteShader(int handle) {
        // TODO Auto-generated method stub
        
    }

    public static void glGenTextures(int i, int[] ids, int j) {
        // TODO Auto-generated method stub
        
    }

    public static void glBindTexture(int glTexture2d, int id) {
        // TODO Auto-generated method stub
        
    }

    public static void glActiveTexture(int i) {
        // TODO Auto-generated method stub
        
    }

    public static void glTexParameterf(int glTexture2d, int glTextureMinFilter, int minMode) {
        // TODO Auto-generated method stub
        
    }

    public static void glDeleteTextures(int i, int[] ids, int j) {
        // TODO Auto-generated method stub
        
    }

    public static void glTexImage2D(int glTexture2d, int i, int glRgba, int w, int h, int j, int glRgba2,
            int glUnsignedByte, Object imageBuffer) {
        // TODO Auto-generated method stub
        
    }

    public static void glPixelStorei(int glUnpackAlignment, int i) {
        // TODO Auto-generated method stub
        
    }

    public static void glUniform1i(int location, int value) {
        // TODO Auto-generated method stub
        
    }

    public static void glUniform1f(int location, float value) {
        // TODO Auto-generated method stub
        
    }

    public static void glUniform2f(int location, float v1, float v2) {
        // TODO Auto-generated method stub
        
    }

    public static void glUniform4f(int location, float v1, float v2, float v3, float v4) {
        // TODO Auto-generated method stub
        
    }

    public static void glUniformMatrix3fv(int location, int i, boolean b, float[] value, int j) {
        // TODO Auto-generated method stub
        
    }

    public static void glUniformMatrix4fv(int location, int i, boolean b, float[] value, int j) {
        // TODO Auto-generated method stub
        
    }

    public static void glScissor(int i, int j, int width, int height) {
        // TODO Auto-generated method stub
        
    }

    public static void glClear(int glColorBufferBit) {
        // TODO Auto-generated method stub
        
    }

    public static void glViewport(int i, int j, int width, int height) {
        // TODO Auto-generated method stub
        
    }

    public static void glEnable(int glBlend) {
        // TODO Auto-generated method stub
        
    }

    public static void glBlendFunc(int glSrcAlpha, int glOneMinusSrcAlpha) {
        // TODO Auto-generated method stub
        
    }

    public static void glDrawElements(int glTriangles, int size, int glUnsignedShort, ShortBuffer indices) {
        // TODO Auto-generated method stub
        
    }

}
