package spring.ecommerce.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import spring.ecommerce.entity.ImageEntity;
import spring.ecommerce.service.ImageService;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ImageController imageController;

    private ImageEntity imageEntity;

    @BeforeEach
    void setUp() {
        imageEntity = new ImageEntity();
        imageEntity.setId(1L);
        imageEntity.setName("testImage.jpg");
        imageEntity.setPicByte(new byte[]{1, 2, 3, 4});
    }

    @Test
    void testUploadImage() {
        MultipartFile file = mock(MultipartFile.class);
        when(imageService.saveImage(file)).thenReturn(imageEntity);
        
        ResponseEntity<?> response = imageController.uploadImage(file);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(imageEntity, response.getBody());
    }

    @Test
    void testGetImageAsBase64_Found() {
        when(imageService.getImageById(1L)).thenReturn(Optional.of(imageEntity));
        
        ResponseEntity<String> response = imageController.getImageAsBase64(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Base64.getEncoder().encodeToString(imageEntity.getPicByte()), response.getBody());
    }

    @Test
    void testGetImageAsBase64_NotFound() {
        when(imageService.getImageById(1L)).thenReturn(Optional.empty());
        
        ResponseEntity<String> response = imageController.getImageAsBase64(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetImageByName_Found() {
        when(imageService.getImageByName("testImage.jpg")).thenReturn(Optional.of(imageEntity));
        
        ResponseEntity<?> response = imageController.getImageByName("testImage.jpg");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(imageEntity, response.getBody());
    }

    @Test
    void testGetImageByName_NotFound() {
        when(imageService.getImageByName("testImage.jpg")).thenReturn(Optional.empty());
        
        ResponseEntity<?> response = imageController.getImageByName("testImage.jpg");
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testDeleteImage() {
        doNothing().when(imageService).removeImage(1L);
        
        ResponseEntity<?> response = imageController.deleteImage(1L);
        
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testGetAllImages() {
        when(imageService.getAllImages()).thenReturn(List.of(imageEntity));
        
        ResponseEntity<List<ImageEntity>> response = imageController.getAllImages();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(imageEntity, response.getBody().get(0));
    }
}
