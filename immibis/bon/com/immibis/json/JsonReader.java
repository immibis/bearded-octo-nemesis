package immibis.bon.com.immibis.json;

import java.io.EOFException;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Valid JSON values are Maps (from Strings to JSON values), Lists (of JSON values), Booleans, Strings, Doubles and null.
 */
public final class JsonReader {
	
	public static Object readJSON(Reader in) throws IOException {
		return readJSONInternal(new PushbackReader(in));
	}
	
	private static char readNoWS(PushbackReader in) throws IOException {
		while(true) {
			char ch = read(in);
			if(ch != 0x20 && ch != 0x09 && ch != 0x0A && ch != 0x0D)
				return ch;
		}
	}
	
	private static char read(PushbackReader in) throws IOException {
		int ch = in.read();
		if(ch == -1)
			throw new EOFException();
		return (char)ch;
	}
	
	private static int fromHexChar(char ch) throws IOException {
		if(ch >= '0' && ch <= '9')
			return ch - '0';
		if(ch >= 'A' && ch <= 'F')
			return ch - 'A' + 10;
		if(ch >= 'a' && ch <= 'f')
			return ch - 'a' + 10;
		throw new IOException("invalid hexadecimal character: "+ch);
	}

	private static String readString(PushbackReader in) throws IOException {
		StringBuilder sb = new StringBuilder();
		
		if(readNoWS(in) != '"')
			throw new IOException("expected \" to start string");
		
		while(true) {
			char ch = read(in);
			if(ch == '"')
				break;
			
			if(ch == '\\') {
				ch = read(in);
				switch(ch) {
				case '"':
				case '\\':
				case '/':
					sb.append(ch);
					break;
				case 'b':
					sb.append('\b');
					break;
				case 'f':
					sb.append('\f');
					break;
				case 'n':
					sb.append('\n');
					break;
				case 'r':
					sb.append('\r');
					break;
				case 't':
					sb.append('\t');
					break;
				case 'u':
					int value = (fromHexChar(read(in)) << 12) | (fromHexChar(read(in)) << 8) | (fromHexChar(read(in)) << 4) | fromHexChar(read(in));
					sb.append((char)value);
					break;
				default:
					throw new IOException("Invalid JSON string escape sequence: \\"+ch);
				}
			} else
				sb.append(ch);
		}
		
		return sb.toString();
	}
	
	private static Object readJSONInternal(PushbackReader in) throws IOException {
		char firstChar = readNoWS(in);
		
		if(firstChar == 't') {
			if(read(in) != 'r' || read(in) != 'u' || read(in) != 'e') throw new IOException("expected 'rue' after 't'");
			return Boolean.TRUE;
			
		} else if(firstChar == 'f') {
			if(read(in) != 'a' || read(in) != 'l' || read(in) != 's' || read(in) != 'e') throw new IOException("expected 'alse' after 'f'");
			return Boolean.FALSE;
			
		} else if(firstChar == 'n') {
			if(read(in) != 'u' || read(in) != 'l' || read(in) != 'l') throw new IOException("expected 'ull' after 'n'");
			return null;
			
		} else if(firstChar == '"') {
			in.unread(firstChar);
			return readString(in);
			
		} else if((firstChar >= '0' && firstChar <= '9') || firstChar == '-') {
			
			StringBuilder asString = new StringBuilder();
			
			if(firstChar == '-')
				asString.append('-');
			else
				in.unread(firstChar);
			
			char ch = read(in);
			asString.append(ch);
			
			if(ch == '0') {
				ch = read(in);
			} else {
				ch = read(in);
				while(ch >= '0' && ch <= '9') {
					asString.append(ch);
					ch = read(in);
				}
			}
			
			if(ch == '.') {
				asString.append(ch);
				ch = read(in);
				if(ch < '0' || ch > '9')
					throw new IOException("expected digits after .");
				while(ch >= '0' && ch <= '9') {
					asString.append(ch);
					ch = read(in);
				}
			}
			
			if(ch == 'e' || ch == 'E') {
				asString.append(ch);
				
				ch = read(in);
				if(ch == '+' || ch == '-') {
					asString.append(ch);
					ch = read(in);
				}
				if(ch < '0' || ch > '9')
					throw new IOException("expected digits in exponent");
				while(ch >= '0' && ch <= '9') {
					asString.append(ch);
					ch = read(in);
				}
				
			}
			
			in.unread(ch);
			
			return Double.parseDouble(asString.toString());
			
		} else if(firstChar == '[') {
			ArrayList<Object> rv = new ArrayList<>();
			
			char nextChar = readNoWS(in);
			if(nextChar == ']')
				return rv;
			in.unread(nextChar);
			
			while(true) {
				rv.add(readJSONInternal(in));
				
				nextChar = readNoWS(in);
				if(nextChar == ']')
					break;
				else if(nextChar != ',')
					throw new IOException("expected ] or , after value in array");
			}
			
			return rv;
		
		} else if(firstChar == '{') {
			
			HashMap<String, Object> rv = new HashMap<>();
			
			char nextChar = readNoWS(in);
			if(nextChar == '}')
				return rv;
			in.unread(nextChar);
			
			while(true) {
				String name = readString(in);
				if(readNoWS(in) != ':')
					throw new IOException("expected : between name and value");
				
				Object value = readJSONInternal(in);
				rv.put(name, value);
				
				nextChar = readNoWS(in);
				if(nextChar == '}')
					break;
				else if(nextChar != ',')
					throw new IOException("expected } or , after value in object");
			}
			
			return rv;
			
		} else {
			throw new IOException("invalid start of json value: "+firstChar);
		}
	}
}
