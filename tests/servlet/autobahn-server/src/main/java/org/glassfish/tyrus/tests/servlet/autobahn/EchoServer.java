/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.tyrus.tests.servlet.autobahn;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.websocket.Session;
import javax.websocket.WebSocketMessage;
import javax.websocket.server.DefaultServerConfiguration;
import javax.websocket.server.WebSocketEndpoint;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
@WebSocketEndpoint(value = "/echo", configuration = DefaultServerConfiguration.class)
public class EchoServer {

    @WebSocketMessage
    public String onText(String text) {
        return text;
    }

    private String message = "";

    @WebSocketMessage
    public void onPartialText(Session session, String text, boolean last) {
        if(last) {
            try {
                session.getRemote().sendString(message + text);
            } catch (IOException e) {
                //
            }
            message = "";
        } else {
            message = message + text;
        }
    }

    @WebSocketMessage
    public ByteBuffer onBinary(ByteBuffer binary) {
        return binary;
    }

    private ByteBuffer buffer = null;

    @WebSocketMessage
    public void onPartialBinary(Session session, ByteBuffer binary, boolean last) {
        if(last) {
            try {
                session.getRemote().sendBytes(joinBuffers(buffer, binary));
            } catch (IOException e) {
                //
            }
            buffer = null;
        } else {
            if(buffer == null) {
                buffer = binary;
            } else {
                buffer = joinBuffers(buffer, binary);
            }

        }
    }

    private ByteBuffer joinBuffers(ByteBuffer bb1, ByteBuffer bb2) {

        final int remaining1 = bb1.remaining();
        final int remaining2 = bb2.remaining();
        byte[] array = new byte[remaining1 + remaining2];
        bb1.get(array, 0, remaining1);
        System.arraycopy(bb2.array(), 0, array, remaining1, remaining2);


        ByteBuffer buf = ByteBuffer.wrap(array);
        buf.limit(remaining1 + remaining2);

        return buf;
    }
}