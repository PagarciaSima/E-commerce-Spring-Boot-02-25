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
import spring.ecommerce.entity.ImageEntity;

@Service
@AllArgsConstructor
@Slf4j
public class ImageService {

    private final ImageDao imageDao;

    /**
     * Saves an image to the database.
     * <p>
     * This method receives an image file, extracts relevant information such as the file name, content type, and byte data,
     * then creates an {@link ImageEntity} object and saves it to the database using the {@link imageDao}. 
     * If there is an error during the file processing or saving, an exception is logged and rethrown as a runtime exception.
     * </p>
     *
     * @param file the image file to be saved
     * @return the saved {@link ImageEntity} object
     * @throws RuntimeException if there is an error saving the image to the database
     */
    public ImageEntity saveImage(MultipartFile file) {
        try {
            log.info("Saving image: {}", file.getOriginalFilename());

            ImageEntity image = new ImageEntity(
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

    /**
     * Retrieves an image from the database by its ID.
     * <p>
     * This method attempts to fetch an {@link ImageEntity} object from the database using the provided ID. 
     * If the image is found, it returns an {@link Optional} containing the image. 
     * If an error occurs during the fetch process, it logs the error and throws a runtime exception.
     * </p>
     *
     * @param id the ID of the image to be retrieved
     * @return an {@link Optional} containing the found {@link ImageEntity} if present, or an empty {@link Optional} if not found
     * @throws RuntimeException if there is an error fetching the image from the database
     */
    public Optional<ImageEntity> getImageById(Long id) {
        try {
            log.info("Fetching image by ID: {}", id);
            return this.imageDao.findById(id);
        } catch (Exception e) {
            log.error("Error fetching image by ID: {}", e.getMessage());
            throw new RuntimeException("Error fetching image.");
        }
    }

    /**
     * Retrieves an image from the database by its name.
     * <p>
     * This method attempts to fetch an {@link ImageEntity} object from the database using the provided name. 
     * If the image is found, it returns an {@link Optional} containing the image. 
     * If an error occurs during the fetch process, it logs the error and throws a runtime exception.
     * </p>
     *
     * @param name the name of the image to be retrieved
     * @return an {@link Optional} containing the found {@link ImageEntity} if present, or an empty {@link Optional} if not found
     * @throws RuntimeException if there is an error fetching the image from the database
     */
    public Optional<ImageEntity> getImageByName(String name) {
        try {
            log.info("Fetching image by name: {}", name);
            return imageDao.findByName(name);
        } catch (Exception e) {
            log.error("Error fetching image by name: {}", e.getMessage());
            throw new RuntimeException("Error fetching image.");
        }
    }
    
    /**
     * Removes an image from the database by its ID.
     * <p>
     * This method first deletes any relationships between the image and products in the 
     * {@code product_images} table by calling {@link imageDao#deleteFromProductImagesByImageID(Long)}. 
     * Afterward, it deletes the image from the {@code image} table using the provided image ID.
     * </p>
     * <p>
     * This operation is performed within a transaction to ensure that both deletions are successful 
     * and consistent. If any part of the process fails, the transaction is rolled back.
     * </p>
     *
     * @param imageId the ID of the image to be removed
     */
    @Transactional
    public void removeImage(Long imageId) {
        // Elimina las relaciones en la tabla product_images
    	this.imageDao.deleteFromProductImagesByImageID(imageId);

        // Elimina la imagen de la tabla image
        this.imageDao.deleteById(imageId);
    }

    /**
     * Retrieves all images stored in the database.
     *
     * This method fetches all images from the {@code imageDao} repository.
     * If an error occurs during the retrieval process, a {@link RuntimeException} is thrown.
     *
     * @return A {@link List} of {@link ImageEntity} objects representing all stored images.
     * @throws RuntimeException if an error occurs while retrieving the images.
     */
    public List<ImageEntity> getAllImages() {
        try {
            log.info("Fetching all images.");
            return (List<ImageEntity>) this.imageDao.findAll();
        } catch (Exception e) {
            log.error("Error fetching all images: {}", e.getMessage());
            throw new RuntimeException("Error fetching all images.");
        }
    }
}
