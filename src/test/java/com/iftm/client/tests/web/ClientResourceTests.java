package com.iftm.client.tests.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iftm.client.dto.ClientDTO;
import com.iftm.client.services.ClientService;
import com.iftm.client.services.exceptions.DatabaseException;
import com.iftm.client.services.exceptions.ResourceNotFoundException;
import com.iftm.client.tests.factory.ClientFactory;

@SpringBootTest
@AutoConfigureMockMvc
public class ClientResourceTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ClientService service;

	@Autowired
	private ObjectMapper objectMapper;

	private Long existingId;
	private Long nonExistingId;
	private Long dependentId;
	private ClientDTO clientDTO;
	private ClientDTO newClientDTO;
	private List<ClientDTO> list;
	private PageImpl<ClientDTO> page;

	@BeforeEach
	void setUp() throws Exception {

		existingId = 1L;
		nonExistingId = 1000L;
		dependentId = 4L;
		clientDTO = ClientFactory.createClientDTO();
		newClientDTO = ClientFactory.createClientDTO(null);
		list = new ArrayList<ClientDTO>();
		page = new PageImpl<>(List.of(clientDTO));

		when(service.findById(existingId)).thenReturn(clientDTO);
		when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);

		when(service.findAll()).thenReturn(list);
		when(service.findAllPaged(any())).thenReturn(page);

		when(service.insert(any())).thenReturn(clientDTO);

		when(service.update(eq(existingId), any())).thenReturn(clientDTO);
		when(service.update(eq(nonExistingId), any())).thenThrow(ResourceNotFoundException.class);

		doNothing().when(service).delete(existingId);
		doThrow(ResourceNotFoundException.class).when(service).delete(nonExistingId);
		doThrow(DatabaseException.class).when(service).delete(dependentId);
	}

	/*
	 * insert deveria retornar “created” (código 201), bem como um produto criado,
	 * quando os dados forem válidos
	 */
	@Test
	public void insertShouldReturnCod201AndClientDTOWhenIdExists() throws Exception {

		String jsonBody = objectMapper.writeValueAsString(newClientDTO);
		ResultActions result = mockMvc.perform(put("/clients/{id}", existingId).content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
	}

	/*
	 * delete deveria ○ retornar “no content” (código 204) quando o id existir
	 */

	@Test
	public void DeleteShouldReturnNoContentWhenIdExists() throws Exception {

		String jsonBody = objectMapper.writeValueAsString(newClientDTO);

		ResultActions result = mockMvc.perform(delete("/clients/{id}", existingId).content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().is(204));
		result.andExpect(jsonPath("$.id").doesNotExist());

	}

	/* delete deveria ○ retornar “not found” (código 404) quando o id não existir */

	@Test
	public void DeleteShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {

		String jsonBody = objectMapper.writeValueAsString(newClientDTO);

		ResultActions result = mockMvc.perform(delete("/clients/{id}", nonExistingId).content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isNotFound());

	}

	/*
	 * update ○ retornar “ok” (código 200), bem como o produto atualizado para um id
	 * existente.
	 */

	@Test
	public void updateShouldReturnClientDTOWhenIdExists() throws Exception {

		String jsonBody = objectMapper.writeValueAsString(newClientDTO);

		String expectedName = newClientDTO.getName();
		Double expectedIncome = newClientDTO.getIncome();

		ResultActions result = mockMvc.perform(put("/clients/{id}", existingId).content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.id").value(existingId));
		result.andExpect(jsonPath("$.name").value(expectedName));
		result.andExpect(jsonPath("$.income").value(expectedIncome));
	}

	/*
	 * retornar “not found” (código 204) quando o id não existir. Fazer uma
	 * assertion para verificar no json de retorno se o campo “error” contém a
	 * string “Resource not found”
	 */
	@Test
	public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {

		String jsonBody = objectMapper.writeValueAsString(newClientDTO);
		
		String expectedMesage = "Resource not found";
		
		ResultActions result = mockMvc.perform(put("/clients/{id}", nonExistingId).content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isNotFound());
		result.andExpect(jsonPath("$.error").value(expectedMesage));
		
	}


}
