package org.fnppl.opensdx.file_transfer.errors.exceptions;

public class WrongFileSizeException extends OSDXException{
	private static final long serialVersionUID = 3523835242053317120L;

	public WrongFileSizeException(String msg){
		super(msg);
	}
}
