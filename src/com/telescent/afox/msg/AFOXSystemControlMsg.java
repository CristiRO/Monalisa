package com.telescent.afox.msg;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import com.telescent.afox.utils.AFOXMsgs;
import com.telescent.afox.utils.FlatSerialize;
import com.telescent.afox.utils.UnflatSerialize;

public class AFOXSystemControlMsg implements Serializable, IHeader {

	private static final long serialVersionUID = -3114004379886755374L;

	public int Header = getHeader();  
	public int Function;
	public String UserName;
	public String Password;

	private AFOXSystemControlMsg() { }

	public AFOXSystemControlMsg(
			String UserName_in,
			int Function_in, 
			String Password_in){ 
		Function = Function_in;
		UserName = UserName_in;
		Password = Password_in;
	}

	public ByteArrayOutputStream Serialize() {
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		byte[] b = this.ToFlatSer();
		if (b == null) b = new byte[0];
		try {
			o.write(b);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return o;
	}

	public static AFOXSystemControlMsg DeSerialize(ByteArrayOutputStream ms) {
		AFOXSystemControlMsg ci = FromFlatSer(ms.toByteArray());
		return ci;
	}

	public static AFOXSystemControlMsg DeSerialize(byte[] b) {
		AFOXSystemControlMsg ci = FromFlatSer(b);
		return ci;
	}

	/***************************************************************************
	 *                                                                          *
	 *  Custom Memory Efficient Serializer/Deserialzer                          *
	 *  All variables very flat.  No headers etc.                               *
	 *                                                                          *
	 *  byte[] ToFlatSer() : returns byte array of serialized data              *
	 *                                                                          *  
	 *  FromFlatSer(UnflatSerialize uf) : returns an instance of this class     *
	 *    when supplied with the flattened serialized data stream.  uf has      *
	 *    its own internal pointer and will start in the data stream where      *
	 *    it left off in any previous call.  An error will be returned if       *
	 *    uf cannot deserialize an input.  If a deserialization failure         *
	 *    occurs, uf will no longer deseriaize data.  Therefore at the first    *
	 *    failure, the operation should be abondened.                           *
	 *                                                                          *
	 ***************************************************************************/
	//Serialzer   
	public byte[] ToFlatSer() {
		FlatSerialize f = new FlatSerialize();
		Object o[]= new Object[4];
		o[0] = Header;
		o[1] = UserName;
		o[2] = Function;
		o[3] = Password;
		f.AddItems(o);
		return f.serializedData;
	}

	//Deserialzer
	//extract and populate structure and return the rest of the byte stream
	public static AFOXSystemControlMsg FromFlatSer(byte[] ba) {
		UnflatSerialize uf = new UnflatSerialize(ba);
		return(FromFlatSer(uf));
	} 

	public static AFOXSystemControlMsg FromFlatSer(UnflatSerialize uf) {
		AFOXSystemControlMsg ci = null;  // ci = class instance.  init to null in case of error later so we have our, (defalut-error), return value.

		/* 1)
		 * copy the constructor variables here.  This class is entirely populated by
		 * the constructor, and we will instantiate an instance here, (to return to the
		 * caller) by using the inputs just below that will be populated as uf below
		 * is deserialized into them.
		 */
		String UserName_in;
		int Function_in;
		String Password_in;
		/* 2) get each item, (deserialize each item), in the proper order, (the same way 
		 * they were serialized).
		 * Check each "get" for success or failure.  If it fails, abondon the
		 * the rest of the deserialization and return an error.
		 */

		boolean success;
		while(true) { //while loop only done one time.  a "trick" for quick branching out if any step fails.
			int headerDummy;  //Need to read header, but we don't use it later to instantiate.
			
			success = uf.getInt();     
			if(!success) break;
			headerDummy = uf.intVal;
			
			success = uf.getString();     
			if(!success) break;
			UserName_in = uf.stringVal;

			success = uf.getInt();     
			if(!success) break;
			Function_in = uf.intVal;
			
			success = uf.getString();     
			if(!success) break;
			Password_in = uf.stringVal;

			//Check we have exactly 0 bytes left or we have an error
			success = uf.getNumBytesLeft() == 0; if(!success) break;

			if(success)  //must be success if we made it here
			{ ci = new AFOXSystemControlMsg(
					UserName_in,
					Function_in,
					Password_in
			);
			}

			break;  //always finish with break so we only do the while loop once.

		}
		return ci;
	}


	public int getHeader() {
		return AFOXMsgs.TCP_AFOXSystemControlMsg; 
	}

}
