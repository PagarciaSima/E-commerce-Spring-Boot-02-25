package spring.ecommerce.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.dao.ImageDao;
import spring.ecommerce.model.Image;

@Service
@AllArgsConstructor
@Slf4j
public class ImageService {

    private final ImageDao imageDao;

    public Image saveImage(MultipartFile file) {
        try {
            log.info("Saving image: {}", file.getOriginalFilename());

            Image image = new Image(
                file.getOriginalFilename(), 
                file.getOriginalFilename(), 
                file.getContentType(), 
                file.getBytes()
            );

            return imageDao.save(image);
        } catch (IOException e) {
            log.error("Error saving image: {}", e.getMessage());
            throw new RuntimeException("Error saving image.", e);
        }
    }


    public Optional<Image> getImageById(Long id) {
        try {
            log.info("Fetching image by ID: {}", id);
            return imageDao.findById(id);
        } catch (Exception e) {
            log.error("Error fetching image by ID: {}", e.getMessage());
            throw new RuntimeException("Error fetching image.");
        }
    }

    public Optional<Image> getImageByName(String name) {
        try {
            log.info("Fetching image by name: {}", name);
            return imageDao.findByName(name);
        } catch (Exception e) {
            log.error("Error fetching image by name: {}", e.getMessage());
            throw new RuntimeException("Error fetching image.");
        }
    }
    

    @Transactional
    public void removeImage(Long imageId) {
        // Elimina las relaciones en la tabla product_images
    	imageDao.deleteFromProductImagesByImageID(imageId);

        // Elimina la imagen de la tabla image
        imageDao.deleteById(imageId);
    }

    public List<Image> getAllImages() {
        try {
            log.info("Fetching all images.");
            return (List<Image>) imageDao.findAll();
        } catch (Exception e) {
            log.error("Error fetching all images: {}", e.getMessage());
            throw new RuntimeException("Error fetching all images.");
        }
    }
}
