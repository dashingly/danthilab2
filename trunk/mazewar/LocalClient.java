/*
Copyright (C) 2004 Geoffrey Alan Washburn
    
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
    
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
    
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
USA.
*/
  
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * An abstract class for {@link Client}s in a {@link Maze} that local to the 
 * computer the game is running upon. You may choose to implement some of 
 * your code for communicating with other implementations by overriding 
 * methods in {@link Client} here to intercept upcalls by {@link GUIClient} and 
 * {@link RobotClient} and generate the appropriate network events.
 * @author Geoffrey Washburn &lt;<a href="mailto:geoffw@cis.upenn.edu">geoffw@cis.upenn.edu</a>&gt;
 * @version $Id: LocalClient.java 343 2004-01-24 03:43:45Z geoffw $
 */


public abstract class LocalClient extends Client {

        /** 
         * Create a {@link Client} local to this machine.
         * @param name The name of this {@link Client}.
         */
        public LocalClient(String name) {
                super(name);
                assert(name != null);
        }
		
		/**
         * Add an object to be notified when this {@link Client} performs an 
         * action.
         * @param cl An object that implementing the {@link ClientListener cl}
         * interface.
         */
        public void addClientListener(ClientListener cl) {
                assert(cl != null);
                listenerSet.add(cl);
        }
        
        /**
         * Remove an object from the action notification queue.
         * @param cl The {@link ClientListener} to remove.
         */
        public void removeClientListener(ClientListener cl) {
                listenerSet.remove(cl);
        }

        /**
         * Move the client forward.
         * @return <code>true</code> if move was successful, otherwise <code>false</code>.
         */
        protected boolean forward() {
				notifyMoveForward();
				return true;
        }
        
        /**
         * Move the client backward.
         * @return <code>true</code> if move was successful, otherwise <code>false</code>.
         */
        protected boolean backup() {
                notifyMoveBackward();
				return true;
        }
        
        /**
         * Turn the client ninety degrees counter-clockwise.
         */
        protected void turnLeft() {
                notifyTurnLeft();
        }
        
        /**
         * Turn the client ninety degrees clockwise.
         */
        protected void turnRight() {
                notifyTurnRight();
        }
        
        /**
         * Fire a projectile.
         * @return <code>true</code> if a projectile was successfully launched, otherwise <code>false</code>.
         */
        protected boolean fire() {
				notifyFire();
				return true;
        }
		
		/** 
         * Notify listeners that the client moved forward.
         */
        private void notifyMoveForward() {
                notifyListeners(ClientEvent.moveForward);
        }
        
        /**
         * Notify listeners that the client moved backward.
         */
        private void notifyMoveBackward() {
                notifyListeners(ClientEvent.moveBackward);
        }
        
        /**
         * Notify listeners that the client turned right.
         */
        private void notifyTurnRight() {
                notifyListeners(ClientEvent.turnRight);
        }
        
        /**
         * Notify listeners that the client turned left.
         */
        private void notifyTurnLeft() {
                notifyListeners(ClientEvent.turnLeft);       
        }
        
        /**
         * Notify listeners that the client fired.
         */
        private void notifyFire() {
                notifyListeners(ClientEvent.fire);       
        }
        
        /**
         * Send a the specified {@link ClientEvent} to all registered listeners
         * @param ce Event to be sent.
         */
        private void notifyListeners(ClientEvent ce) {
                assert(ce != null);
                Iterator i = listenerSet.iterator();
                while (i.hasNext()) {
                        Object o = i.next();
                        assert(o instanceof ClientListener);
                        ClientListener cl = (ClientListener)o;
                        cl.clientUpdate(this, ce);
                } 
        }
		
		/* Internals ******************************************************/        
        

        /**
         * Maintain a set of listeners.
         */
        private Set listenerSet = new HashSet();
        
        /**
         * Name of the client.
         */
        private String name = null;
}
