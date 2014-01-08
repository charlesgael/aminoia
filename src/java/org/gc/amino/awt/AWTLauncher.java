package org.gc.amino.awt;

import javax.swing.JFrame;

public class AWTLauncher extends JFrame {

    public AWTLauncher() {

        add( new Board() );

        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        setSize( 900, 600 );
        setLocationRelativeTo( null );
        setTitle( "AWTLauncher" );
        setResizable( false );
        setVisible( true );

    }

    public static void main( String[] args ) {
        new AWTLauncher();
    }
}