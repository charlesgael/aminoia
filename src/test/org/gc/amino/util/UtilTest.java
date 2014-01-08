package org.gc.amino.util;

import org.junit.*;
import static org.junit.Assert.*;

public class UtilTest {
    
    @Before
    public void init() {
         Util.initForUnitTesting();
    }
    
    @Test
    public void angleAdd() {
        assertEquals( Math.PI/2, Util.angleAdd( Math.PI/2, 0 ) );
        assertEquals( -Math.PI/2, Util.angleAdd( Math.PI*3/4, Math.PI*3/4 ) );
        assertEquals( Math.PI/2, Util.angleAdd( -Math.PI*3/4, -Math.PI*3/4 ) );
    }

    @Test
    public void angleDiff() {
        assertEquals( Math.PI/2, Util.angleDiff( Math.PI/2, 0 ) );
        assertEquals( Math.PI/4, Util.angleDiff( Math.PI/2, Math.PI/4 ) );
        assertEquals( Math.PI, Util.angleDiff( -Math.PI/2, Math.PI/2 ) );
        assertEquals( Math.PI, Util.angleDiff( Math.PI/2, -Math.PI/2 ) );
        assertEquals( Math.PI/2, Util.angleDiff( -Math.PI*3/4, Math.PI*3/4 ) );
        assertEquals( -Math.PI/2, Util.angleDiff( Math.PI*3/4, -Math.PI*3/4 ) );
    }
    
}
