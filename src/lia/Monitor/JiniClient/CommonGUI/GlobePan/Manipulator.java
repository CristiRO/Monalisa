package lia.Monitor.JiniClient.CommonGUI.GlobePan;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public abstract class Manipulator implements MouseListener,
                                             MouseMotionListener,
                                             MouseWheelListener,
                                             KeyListener
{

  public void mouseClicked(MouseEvent e) {}
  public void mouseDragged(MouseEvent e) {}
  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}
  public void mouseMoved(MouseEvent e) {}
  public void mousePressed(MouseEvent e) {}
  public void mouseReleased(MouseEvent e) {}
  public void mouseWheelMoved(MouseWheelEvent e) {}
  public void keyPressed(KeyEvent e) {}
  public void keyReleased(KeyEvent e) {}
  public void keyTyped(KeyEvent e) {}
 
}
