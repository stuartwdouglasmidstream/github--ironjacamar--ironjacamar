/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.jca.core.workmanager.selector;

import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.spi.workmanager.Address;

import java.util.Map;

import javax.resource.spi.work.DistributableWork;

import org.jboss.logging.Logger;

/**
 * The max free threads selector
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class MaxFreeThreads extends AbstractSelector
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, MaxFreeThreads.class.getName());

    /**
    * Constructor
    */
   public MaxFreeThreads()
   {
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public synchronized Address selectDistributedWorkManager(Address own, DistributableWork work)
   {
      /*
        TODO
      String value = getWorkManager(work);
      if (value != null)
      {
         if (trace)
            log.tracef("WorkManager: %s", value);

         return value;
      }
      */

      Map<Address, Long> selectionMap = getSelectionMap(own.getWorkManagerId(), work);
      Address result = null;
      long freeThread = 0L;

      if (selectionMap != null)
      {
         for (Map.Entry<Address, Long> entry : selectionMap.entrySet())
         {
            Address id = entry.getKey();
            if (!own.equals(id))
            {
               Long free = entry.getValue();
               if (free != null && free.longValue() > 0)
               {
                  if (free.longValue() > freeThread)
                  {
                     result = id;
                     freeThread = free.longValue();
                  }
               }
            }
         }
      }

      log.tracef("WorkManager: %s (%s)", result, freeThread);

      return result;
   }
}
