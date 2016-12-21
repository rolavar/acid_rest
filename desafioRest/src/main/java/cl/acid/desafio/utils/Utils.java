package cl.acid.desafio.utils;

public class Utils
{
	public static class Mensajes{
		public static final String OK = "Ok";
		public static final String BAD_REQUEST = "Bad request";
		public static final String UNAUTHORIZED = "Unauthorized";
	}
	
	public static class Codigos{
		public static final String OK = "201";
		public static final String BAD_REQUEST = "400";
		public static final String UNAUTHORIZED = "401";
		
		public static String getCodigoByMsg(String msg)
		{
			String retorno = "-1";
			switch(msg)
			{
				case "Ok": 
					retorno = OK;
					break;
				case "Bad request":
					retorno = BAD_REQUEST;
					break;
				case "Unauthorized":
					retorno = UNAUTHORIZED;
					break;
				
			}
			return retorno;
			
		}
	}
	
	
}
