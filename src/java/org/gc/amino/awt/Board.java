package org.gc.amino.awt;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.Timer;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.gc.amino.engine.Game;
import org.gc.amino.engine.mote.Mote;
import org.gc.amino.engine.terrainmap.PointD;
import org.gc.amino.engine.terrainmap.TerrainMap;
import org.gc.amino.util.Util;

public class Board extends JPanel implements KeyListener, ActionListener, MouseListener, MouseWheelListener {

    private static final Logger log = Logger.getLogger( Board.class );
    
    protected TerrainMap terrainMap;
    private Mote me;
    private Timer timer;
    private Set<String> keyPressed = new HashSet<String>();
    protected double terrainShiftDisplayWidth = 0, terrainShiftDisplayHeight = 0;
    protected boolean paused = false;
    protected int boardDisplayWidth = -1, boardDisplayHeight;
    protected double zoomFactor = 1;
    private PointD mousePressedAt = new PointD( -1, -1 );
    private boolean win = false;
    
    public Board() {
        setDoubleBuffered( true );
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel( Level.INFO );
        PatternLayout pa = new PatternLayout();
        pa.setConversionPattern( "%d{ISO8601} %-5p %c{1}:%L(%M) - %m%n" );
        ( (Appender) Logger.getRootLogger().getAllAppenders().nextElement() ).setLayout( pa );
        
        terrainMap = new TerrainMap( new PointD( 880, 560 ) );
        Game.init( terrainMap );

        me = new Mote( "me", new PointD( terrainMap.getMax().x / 3, terrainMap.getMax().y / 2 ), terrainMap.getMax().x / 30 );
        me.setColor( 50, 50, 250 );
        //Game.addMote( me );
        Game.addExternallyControlledMote( me, "http://localhost:1234/" );

        Mote ia = new Mote( "ia", new PointD( terrainMap.getMax().x * 2 / 3, terrainMap.getMax().y / 2 ), terrainMap.getMax().x / 30 );
        ia.setColor( 50, 250, 50 ); 
        Game.addExternallyControlledMote( ia, "http://PC1-INFO8:1234/" );
        
        Random rnd = new Random();
        for ( int i = 0; i < ( terrainMap.getMax().x * terrainMap.getMax().y ) / 10000; i++ ) {
            try {
                PointD position;
                int radius;
              find_location:
                while ( true ) {
                    radius = rnd.nextInt( (int)terrainMap.getMax().x / 20 );
                    position = new PointD( rnd.nextInt( (int)terrainMap.getMax().x - radius * 2 ) + radius,
                                           rnd.nextInt( (int)terrainMap.getMax().y - radius * 2 ) + radius );
                    for ( Mote mote : Game.getMotes() ) {
                        double distance = Math.sqrt( Util.sqr( position.x - mote.getPosition().x ) + Util.sqr( position.y - mote.getPosition().y ) );
                        if ( distance < radius + mote.getRadius() ) {
                            continue find_location;
                        }
                    }
                    break;
                }
                Mote m = Game.createFurnitureMote( position, radius );
                Game.addMote( m );
                m.setSpeed( new PointD( ( rnd.nextDouble() - 0.5 ) / 10, ( rnd.nextDouble() - 0.5 ) / 10 ) );
            } catch ( IllegalArgumentException iae ) {}
        }

        setFocusable( true );
        requestFocusInWindow();
        addKeyListener( this );
        addMouseListener( this );
        addMouseWheelListener( this );
        
        timer = new Timer( 20, this );
        timer.setActionCommand( "FRAME" );
        timer.start();
    }


    public void paint( Graphics g ) {
        super.paint( g );

        if ( boardDisplayWidth == -1 ) {
            boardDisplayWidth = (int)getSize().getWidth();
            boardDisplayHeight = (int)getSize().getHeight();
        }
        
        setBackground( Color.BLACK );
        
        // display motes
        boolean largest = true;
        for ( Mote mote : Game.getMotes() ) {
            g.setColor( new Color( mote.getColor().first, mote.getColor().second, mote.getColor().third ) );
            g.fillOval( (int)terrain2boardX( mote.getPosition().x - mote.getRadius() ),
                        (int)terrain2boardY( mote.getPosition().y - mote.getRadius() ),
                        (int)terrain2boardSize( mote.getRadius() * 2 ),
                        (int)terrain2boardSize( mote.getRadius() * 2 ) );
            if ( mote == me ) {
                g.setColor( Color.WHITE );
            } else if ( mote.getRadius() > me.getRadius() ) {
                g.setColor( Color.RED );
                largest = false;
            } else {
                g.setColor( Color.BLUE );
            }
            g.drawOval( (int)terrain2boardX( mote.getPosition().x - mote.getRadius() ),
                        (int)terrain2boardY( mote.getPosition().y - mote.getRadius() ),
                        (int)terrain2boardSize( mote.getRadius() * 2 ),
                        (int)terrain2boardSize( mote.getRadius() * 2 ) );
        }
        if ( largest ) {
            win = true;
        }

        if ( win ) {
            g.setColor( Color.WHITE );
            drawStringCentered( g, "You win!", 40, boardDisplayWidth / 2, boardDisplayHeight / 2 );
        } else if ( me.isDead() ) {
            g.setColor( Color.RED );
            drawStringCentered( g, "You lose :(", 40, boardDisplayWidth / 2, boardDisplayHeight / 2 );
        }
        
        // borders
        g.setColor( Color.WHITE );
        int[] px = new int[] { (int)terrain2boardX( 0 ),
                               (int)terrain2boardX( terrainMap.getMax().x ),
                               (int)terrain2boardX( terrainMap.getMax().x ),
                               (int)terrain2boardX( 0 ) };
        int[] py = new int[] { (int)terrain2boardY( 0 ),
                               (int)terrain2boardY( 0 ),
                               (int)terrain2boardY( terrainMap.getMax().y ),
                               (int)terrain2boardY( terrainMap.getMax().y ) };
        g.drawPolygon( px, py, 4 );
        
        Toolkit.getDefaultToolkit().sync();
    }

    public void keyTyped( KeyEvent e ) {}

    public void keyPressed( KeyEvent e ) {
        keyPressed.add( KeyDictionary.getKeyText( e.getKeyCode() ) );
    }

    public void keyReleased( KeyEvent e ) {
        keyPressed.remove( KeyDictionary.getKeyText( e.getKeyCode() ) );
        if ( e.getKeyCode() == KeyEvent.VK_SPACE ) {
            paused = ! paused;
        }
    }

    public void actionPerformed( ActionEvent e ) {
        if ( e.getActionCommand().equals( "FRAME" ) ) {
            if ( keyPressed.contains( "Up" ) ) {
                terrainShiftDisplayHeight -= (double)5 / zoomFactor;
            } else if ( keyPressed.contains( "Down" ) ) {
                terrainShiftDisplayHeight += (double)5 / zoomFactor;
            }
            if ( keyPressed.contains( "Left" ) ) {
                terrainShiftDisplayWidth -= (double)5 / zoomFactor;
            } else if ( keyPressed.contains( "Right" ) ) {
                terrainShiftDisplayWidth += (double)5 / zoomFactor;
            }
            Point mouse_position = getMousePosition();
            if ( mousePressedAt.x != -1 && mouse_position != null ) {
                terrainShiftDisplayWidth -= board2terrainSize( mouse_position.getX() - mousePressedAt.x );
                terrainShiftDisplayHeight -= board2terrainSize( mouse_position.getY() - mousePressedAt.y );
                mousePressedAt.set( mouse_position.getX(), mouse_position.getY() );
            }
            reframeShiftDisplays();
            if ( ! paused ) {
                Game.iterateGame();
            }
            repaint();
        } else {
            log.warn( "unknown actionevent " + e );
        }
    }

    @Override
    public void mouseClicked( MouseEvent e ) {
        if ( e.getButton() == MouseEvent.BUTTON1 ) {
            if ( ! me.isDead() ) { 
                me.move( new PointD( board2terrainX( e.getPoint().getX() ), board2terrainY( e.getPoint().getY() ) ) );
            }
        }
    }

    public double board2terrainX( double x ) {
        return terrainShiftDisplayWidth + x / zoomFactor;
    }
    public double board2terrainY( double y ) {
        return terrainShiftDisplayHeight + y / zoomFactor;  
    }
    public double board2terrainSize( double size ) {
        return size / zoomFactor;
    }

    public double terrain2boardX( double x ) {
        return zoomFactor * ( x - terrainShiftDisplayWidth );  
    }
    public double terrain2boardY( double y ) {
        return zoomFactor * ( y - terrainShiftDisplayHeight );  
    }
    public double terrain2boardSize( double size ) {
        return zoomFactor * size;  
    }
    
    @Override
    public void mousePressed( MouseEvent e ) {
        if ( e.getButton() == MouseEvent.BUTTON1 ) {
            mousePressedAt.set( e.getX(), e.getY() );
        }
    }

    @Override
    public void mouseReleased( MouseEvent e ) {
        mousePressedAt.set( -1, -1 );
    }
        
    @Override
    public void mouseEntered( MouseEvent e ) {}

    @Override
    public void mouseExited( MouseEvent e ) {}

    @Override
    public void mouseWheelMoved( MouseWheelEvent e ) {
        double pointed_x = board2terrainX( e.getPoint().getX() );
        double pointed_y = board2terrainY( e.getPoint().getY() );
        if ( e.getWheelRotation() < 0 && zoomFactor < 20 ) {
            zoomFactor *= 1.3;
        } else if ( e.getWheelRotation() > 0 && zoomFactor > 1 ) {
            zoomFactor /= 1.3;
            if ( zoomFactor < 1 ) {
                zoomFactor = 1;
            }
        }
        terrainShiftDisplayWidth += ( terrain2boardX( pointed_x ) - e.getPoint().getX() ) / zoomFactor;
        terrainShiftDisplayHeight += ( terrain2boardY( pointed_y ) - e.getPoint().getY() ) / zoomFactor;
        reframeShiftDisplays();
    }
    
    private void reframeShiftDisplays() {
        if ( board2terrainX( boardDisplayWidth ) > terrainMap.getMax().x ) {
            terrainShiftDisplayWidth = terrainMap.getMax().x - boardDisplayWidth / zoomFactor + 1; 
        }
        if ( terrainShiftDisplayWidth < 0 ) {
            terrainShiftDisplayWidth = 0;
        }
        if ( board2terrainY( boardDisplayHeight ) > terrainMap.getMax().y ) {
            terrainShiftDisplayHeight = terrainMap.getMax().y - boardDisplayHeight / zoomFactor + 1; 
        }
        if ( terrainShiftDisplayHeight < 0 ) {
            terrainShiftDisplayHeight = 0;
        }
    }    

    public void drawStringCentered( Graphics g, String string, int size, int x, int y ) {
        g.setColor( Color.WHITE );
        g.setFont( new Font( "Sans Serif", Font.BOLD, size ) );
        g.drawString( string, x - g.getFontMetrics().stringWidth( string ) / 2, y + g.getFontMetrics().getAscent() / 3 );
    }
    
}
