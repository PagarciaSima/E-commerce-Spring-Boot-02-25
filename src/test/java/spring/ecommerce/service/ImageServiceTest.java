package spring.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import spring.ecommerce.dao.ImageDao;
import spring.ecommerce.entity.ImageEntity;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageDao imageDao;

    @Mock
    private MultipartFile mockFile;

    @InjectMocks
    private ImageService imageService;

    private ImageEntity mockImage;

    @BeforeEach
    void setUp() {
        // Solo inicializar la imagen simulada
        mockImage = new ImageEntity("test.png", "test.png", "image/png", new byte[]{1, 2, 3, 4});
    }

    @Test
    void testSaveImage() throws IOException {
        // Simulación de archivo dentro de la prueba
        when(mockFile.getOriginalFilename()).thenReturn("test.png");
        when(mockFile.getContentType()).thenReturn("image/png");
        when(mockFile.getBytes()).thenReturn(new byte[]{1, 2, 3, 4});

        when(imageDao.save(any(ImageEntity.class))).thenReturn(mockImage);

        ImageEntity savedImage = imageService.saveImage(mockFile);

        assertNotNull(savedImage);
        assertEquals("test.png", savedImage.getName());
        assertEquals("image/png", savedImage.getType());
        assertNotNull(savedImage.getName());
    }


    @Test
    void testGetImageById() {
        when(imageDao.findById(1L)).thenReturn(Optional.of(mockImage));

        Optional<ImageEntity> foundImage = imageService.getImageById(1L);

        assertTrue(foundImage.isPresent());
        assertEquals("test.png", foundImage.get().getName());
    }

    @Test
    void testGetImageById_NotFound() {
        when(imageDao.findById(1L)).thenReturn(Optional.empty());
        Optional<ImageEntity> foundImage = imageService.getImageById(1L);
        assertFalse(foundImage.isPresent());
    }

    @Test
    void testGetAllImages() {
        when(imageDao.findAll()).thenReturn(List.of(mockImage));

        List<ImageEntity> images = imageService.getAllImages();
        assertFalse(images.isEmpty());
        assertEquals(1, images.size());
    }

    @Test
    void testRemoveImage() {
        // No devuelve nada porque es `void`, solo verificamos que se llame al método
        doNothing().when(imageDao).deleteFromProductImagesByImageID(1L);
        doNothing().when(imageDao).deleteById(1L);

        imageService.removeImage(1L);

        verify(imageDao, times(1)).deleteFromProductImagesByImageID(1L);
        verify(imageDao, times(1)).deleteById(1L);
    }
}
