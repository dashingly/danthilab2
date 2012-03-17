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
 * A skeleton for those {@link Client}s that correspond to clients on other computers.
 * @author Geoffrey Washburn &lt;<a href="mailto:geoffw@cis.upenn.edu">geoffw@cis.upenn.edu</a>&gt;
 * @version $Id: RemoteClient.java 342 2004-01-23 21:35:52Z geoffw $
 */

public class RemoteClient extends Client {
        
        /**
         * Create a remotely controlled {@link Client}.
         * @param name Name of this {@link RemoteClient}.
         */
        public RemoteClient(String name) {
                super(name);
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
