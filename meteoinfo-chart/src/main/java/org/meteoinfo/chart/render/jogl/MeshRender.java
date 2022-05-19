package org.meteoinfo.chart.render.jogl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.fixedfunc.GLPointerFunc;
import com.jogamp.opengl.util.GLBuffers;
import org.meteoinfo.chart.graphic.MeshGraphic;
import org.meteoinfo.chart.jogl.Program;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.chart.jogl.Utils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class MeshRender extends JOGLGraphicRender {

    private MeshGraphic meshGraphic;
    private IntBuffer vbo;
    private IntBuffer vboNormal;
    private Program program;

    /**
     * Constructor
     *
     * @param gl The JOGL GL2 object
     */
    public MeshRender(GL2 gl) {
        super(gl);
    }

    /**
     * Constructor
     *
     * @param gl The opengl pipeline
     * @param meshGraphic The MeshGraphic
     */
    public MeshRender(GL2 gl, MeshGraphic meshGraphic) {
        super(gl);
        this.meshGraphic = meshGraphic;

        initVertexBuffer();

        /*try {
            this.compileShaders();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    private void initVertexBuffer() {
        vbo = GLBuffers.newDirectIntBuffer(1);

        /*FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(meshGraphic.getVertexData(this.transform));
        gl.glGenBuffers(1, vbo);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));
        gl.glBufferData(GL.GL_ARRAY_BUFFER, vertexBuffer.capacity() * Float.BYTES, vertexBuffer, GL.GL_STATIC_DRAW);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);*/

        vboNormal = GLBuffers.newDirectIntBuffer(1);
        /*FloatBuffer normalBuffer = GLBuffers.newDirectFloatBuffer(meshGraphic.getVertexNormal());
        gl.glGenBuffers(1, vboNormal);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboNormal.get(0));
        gl.glBufferData(GL.GL_ARRAY_BUFFER, normalBuffer.capacity() * Float.BYTES, normalBuffer, GL.GL_STATIC_DRAW);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);*/
    }

    @Override
    public void setTransform(Transform transform) {
        super.setTransform(transform);

        float[] vertexData = meshGraphic.getVertexData(this.transform);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl.glGenBuffers(1, vbo);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));
        gl.glBufferData(GL.GL_ARRAY_BUFFER, vertexBuffer.capacity() * Float.BYTES, vertexBuffer, GL.GL_STATIC_DRAW);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

        meshGraphic.calculateNormalVectors(vertexData);
        FloatBuffer normalBuffer = GLBuffers.newDirectFloatBuffer(meshGraphic.getVertexNormal());
        gl.glGenBuffers(1, vboNormal);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboNormal.get(0));
        gl.glBufferData(GL.GL_ARRAY_BUFFER, normalBuffer.capacity() * Float.BYTES, normalBuffer, GL.GL_STATIC_DRAW);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
    }

    void compileShaders() throws Exception {
        String vertexShaderCode = Utils.loadResource("/shaders/mesh/vertex.vert");
        String fragmentShaderCode = Utils.loadResource("/shaders/mesh/mesh.frag");
        program = new Program("mesh", vertexShaderCode, fragmentShaderCode);
    }

    /**
     * Update shaders
     */
    public void updateShaders() {
        if (program == null) {
            try {
                compileShaders();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void setUniforms() {
        program.allocateUniform(gl, "MVP", (gl2, loc) -> {
            gl2.glUniformMatrix4fv(loc, 1, false, this.viewProjMatrix.get(Buffers.newDirectFloatBuffer(16)));
        });
        program.allocateUniform(gl, "MV", (gl2, loc) -> {
            gl2.glUniformMatrix4fv(loc, 1, false, toMatrix(this.mvmatrix).get(Buffers.newDirectFloatBuffer(16)));
        });
        program.allocateUniform(gl, "MVI", (gl2, loc) -> {
            gl2.glUniformMatrix4fv(loc, 1, false, toMatrix(this.mvmatrix).invert().get(Buffers.newDirectFloatBuffer(16)));
        });
        program.allocateUniform(gl, "transMatrix", (gl2, loc) -> {
            gl2.glUniformMatrix4fv(loc, 1, false, this.transform.getTransformMatrix().get(Buffers.newDirectFloatBuffer(16)));
        });
        float[] rgba = meshGraphic.getColor().getRGBComponents(null);
        program.allocateUniform(gl, "color", (gl2, loc) -> {
            gl2.glUniform4f(loc, rgba[0], rgba[1], rgba[2], rgba[3]);
        });

        program.setUniforms(gl);
    }

    @Override
    public void draw() {

        //program.use(gl);
        //setUniforms();

        float[] rgba = meshGraphic.getColor().getRGBComponents(null);
        gl.glColor4fv(rgba, 0);

        /*float[] vertexData = meshGraphic.getVertexData(this.transform);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl.glGenBuffers(1, vbo);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));
        gl.glBufferData(GL.GL_ARRAY_BUFFER, vertexBuffer.capacity() * Float.BYTES, vertexBuffer, GL.GL_STATIC_DRAW);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);*/

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));
        gl.glEnableVertexAttribArray(0);
        gl.glBindAttribLocation(0, 0, "coordinates");
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboNormal.get(0));
        gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
        gl.glNormalPointer(GL.GL_FLOAT, 0, 0);
        //gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboNormal.get(0));
        gl.glEnableVertexAttribArray(1);
        gl.glBindAttribLocation(1, 0, "normal");
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 0, 0);

        gl.glDrawArrays(GL.GL_TRIANGLES, 0, meshGraphic.getVertexNumber());

        gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
        gl.glDisableVertexAttribArray(0);
        gl.glDisableVertexAttribArray(1);

        //gl.glUseProgram(0);
    }
}
