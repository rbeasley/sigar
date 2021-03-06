/*
 * Copyright (C) [2004, 2005, 2006], Hyperic, Inc.
 * This file is part of SIGAR.
 * 
 * SIGAR is free software; you can redistribute it and/or modify
 * it under the terms version 2 of the GNU General Public License as
 * published by the Free Software Foundation. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package org.hyperic.sigar.test;

import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.cmd.Mx;
import org.hyperic.sigar.jmx.SigarProcess;
import org.hyperic.sigar.jmx.SigarRegistry;

public class TestMx extends SigarTestCase {

    public TestMx(String name) {
        super(name);
    }

    public void testRegister() throws Exception {
        try {
            _testRegister();
        } catch (NoClassDefFoundError e) {
            //1.4 jre
            traceln(e + ", skipping");
        }
    }

    private void _testProcess(MBeanServer server) throws Exception {
        long[] pids = getSigar().getProcList();
        for (int i=0; i<pids.length; i++) {
            SigarProcess proc = new SigarProcess();
            proc.setPid(pids[i]);
            ObjectName name;
            try {
                name = new ObjectName(proc.getObjectName());
            } catch (SigarException e) {
                continue; //process may have gone away
            }
            if (server.isRegistered(name)) {
                continue;
            }
            server.registerMBean(proc, name);
        }
    }

    private void _testRegister() throws Exception {
        MBeanServer server;
        try {
            server = Mx.getMBeanServer();
        } catch (SigarException e) {
            traceln(e.getMessage());
            return;
        }

        SigarRegistry rgy = new SigarRegistry(getSigar());
        ObjectName name = new ObjectName(rgy.getObjectName());
        if (!server.isRegistered(name)) {
            server.registerMBean(rgy, name);
        }
        Set beans =
            server.queryNames(new ObjectName("sigar:*"), null);

        assertGtZeroTrace("beans.size", beans.size());

        for (Iterator it=beans.iterator(); it.hasNext();) {
            name = (ObjectName)it.next(); 
            MBeanInfo info = server.getMBeanInfo(name);
            MBeanAttributeInfo[] attrs = info.getAttributes();
            for (int k = 0; k < attrs.length; k++) {
                String attr = attrs[k].getName();
                Object o = server.getAttribute(name, attr);
                traceln(name + ":" + attr + "=" + o);
            }
        }

        _testProcess(server);
    }
}
