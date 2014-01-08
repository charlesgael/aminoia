package org.gc.amino.awt;

import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.HashMap;

public class KeyDictionary {
    private static Map<Integer, String> dictionary;
    static {    
        dictionary = new HashMap<Integer, String>();
        dictionary.put( KeyEvent.VK_F1, "F1" );
        dictionary.put( KeyEvent.VK_F2, "F2" );
        dictionary.put( KeyEvent.VK_F3, "F3" );
        dictionary.put( KeyEvent.VK_F4, "F4" );
        dictionary.put( KeyEvent.VK_F5, "F5" );
        dictionary.put( KeyEvent.VK_F6, "F6" );
        dictionary.put( KeyEvent.VK_F7, "F7" );
        dictionary.put( KeyEvent.VK_F8, "F8" );
        dictionary.put( KeyEvent.VK_F9, "F9" );
        dictionary.put( KeyEvent.VK_F10, "F10" );
        dictionary.put( KeyEvent.VK_F11, "F11" );
        dictionary.put( KeyEvent.VK_F12, "F12" );
        dictionary.put( KeyEvent.VK_LEFT, "Left" );
        dictionary.put( KeyEvent.VK_RIGHT, "Right" );
        dictionary.put( KeyEvent.VK_DOWN, "Down" );
        dictionary.put( KeyEvent.VK_UP, "Up" );
        dictionary.put( KeyEvent.VK_PAGE_DOWN, "Page down" );
        dictionary.put( KeyEvent.VK_PAGE_UP, "Page up" );
        dictionary.put( KeyEvent.VK_DELETE, "Delete" );
        dictionary.put( KeyEvent.VK_BACK_SPACE, "Backspace" );
        dictionary.put( KeyEvent.VK_INSERT, "Insert" );
        dictionary.put( KeyEvent.VK_HOME, "Home" );
        dictionary.put( KeyEvent.VK_END, "End" );
        dictionary.put( KeyEvent.VK_ESCAPE, "Escape" );
        dictionary.put( KeyEvent.VK_TAB, "Tab" );
        dictionary.put( KeyEvent.VK_ENTER, "Enter" );        
    }
    
    public static String getKeyText( int keyCode ) {
        String text = dictionary.get( keyCode );
        if ( text != null ) {
            return text;
        } else {
            return KeyEvent.getKeyText( keyCode );
        }
    }
}
