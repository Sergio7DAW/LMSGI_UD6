/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */

package com.qizx.util.io;



/**
 *    Reads bytes from a remote connection (TODO, this is a mock class)
 *    
 */
public class RemoteByteInput extends CoreByteInput
{
    /*
    interface Pipe extends Remote {
        byte[] getBytes();
    }
   
    public StreamedRemoteByteInput( Pipe pipe );
 */
    

    // temporary implementation
    public RemoteByteInput() { }
    
    public RemoteByteInput( CoreByteOutput source ) {
        super(source);
    }

    public void dump() {
        System.err.println(blockCount+" blocks");
        for(int b = 0; b < blockCount; b++)
            System.err.println(b+" size "+blockSizes[b]);
    }
}
