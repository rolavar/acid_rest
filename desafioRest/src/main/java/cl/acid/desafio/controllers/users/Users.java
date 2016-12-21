package cl.acid.desafio.controllers.users;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;

import cl.acid.desafio.controllers.BaseController;
import cl.acid.desafio.dto.ImagenDto;
import cl.acid.desafio.helpers.ImagenHelper;
import cl.acid.desafio.utils.RespuestaDto;
import cl.acid.desafio.utils.Utils;
import sun.misc.BASE64Decoder;

/**
 * Handles requests for the application home page.
 */
@Controller
@RequestMapping("users")
public class Users extends BaseController
{

	private static final Logger log = LoggerFactory.getLogger(Users.class);
	private final String USER_AUTHORIZED = "usuario1";
	@Autowired
	private ImagenHelper imageHelper;

	/**
	 * Servicio que se encarga de obtener una imagen a traves de su id
	 * @param model
	 * @param id
	 * @param response
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public void getImage(Model model,@PathVariable(value="id") int id, HttpServletResponse response)
	{
		try
		{
			ImagenDto imagen = imageHelper.findImagenById(id);
			if(imagen != null &&
					!imagen.getImage().equals(""))
			{
				BufferedImage imagenToShow = decodeToImage(imagen.getImage());
				response.setContentType("image/png");
				ImageIO.write(imagenToShow, "png", response.getOutputStream());
			}else
			{
				response.getWriter().append("No se encontró la imagen solicitada");
			}
			
			
			
		}catch(Exception ex)
		{
			log.info("Error al mostrar imagen",ex);

		}
		
	}
	
	
	/**
	 * Servicio que se encarga de enviar los datos rescatados de la web para grabar una imagen
	 */
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public String saveImage(Model model, HttpServletResponse response, HttpServletRequest request,
			@RequestBody String json)
	{
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.setSerializationInclusion(Include.NON_EMPTY);
		RespuestaDto respuesta = new RespuestaDto();
		ImagenDto imagen = new ImagenDto();
		try{
			imagen = mapper.readValue(json, ImagenDto.class);
			if ((imagen.getImage() == null || imagen.getImage().trim().equals(""))
					|| (imagen.getUsername() == null || imagen.getUsername().trim().equals("")))
			{
				//	respuesta.setCodigo(Utils.Codigos.BAD_REQUEST);
				respuesta.setMessage(Utils.Mensajes.BAD_REQUEST);
				response.setStatus(Integer.parseInt(Utils.Codigos.BAD_REQUEST));
			} else
			{
				if (imagen.getUsername().equals(USER_AUTHORIZED)
						&& (imagen.getImage() != null && !imagen.getImage().equals("")))
				{
					int id = imageHelper.insertImagen(imagen);
					if (id != -1)
					{
						//respuesta.setCodigo(Utils.Codigos.OK);
						respuesta.setMessage(Utils.Mensajes.OK);
						response.setStatus(Integer.parseInt(Utils.Codigos.OK));
						String uri = request.getScheme() + "://" + 
					             request.getServerName() +      
					             ":" +                          
					             request.getServerPort() +      
					             request.getRequestURI() +
					             "/"+id;
						response.setHeader("location", uri);
						
					} else
					{
						respuesta.setCodigo("0");
						respuesta.setMessage("Problemas con el servicio, intente nuevamente");
					}

				} else if (!imagen.getUsername().equals(USER_AUTHORIZED))
				{
					//respuesta.setCodigo(Utils.Codigos.UNAUTHORIZED);
					respuesta.setMessage(Utils.Mensajes.UNAUTHORIZED);
					response.setStatus(Integer.parseInt(Utils.Codigos.UNAUTHORIZED));

				}
			}
			model.addAttribute("respuesta",mapper.writeValueAsString(respuesta));
		}catch(Exception ex)
		{
			log.info("Problemas en el servicio",ex);
			respuesta.setCodigo("0");
			respuesta.setMessage("Problemas para procesar su solicitud");
			try
			{
				model.addAttribute("respuesta",mapper.writeValueAsString(respuesta));
			} catch (JsonProcessingException e)
			{
				log.error("Error al convertir respuesta",e);
				
			}

		}
		return "home";

	}
	
	/**
	 * Metodo que decodifica un base64 y lo transforma en una imagen tipo BufferedImage
	 * @param imageString
	 * @return
	 */
	private BufferedImage decodeToImage(String imageString)
	{

		BufferedImage image = null;
		byte[] imageByte;
		try
		{
			BASE64Decoder decoder = new BASE64Decoder();
			imageByte = decoder.decodeBuffer(imageString);
			ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
			image = ImageIO.read(bis);
			bis.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return image;
	}

}
