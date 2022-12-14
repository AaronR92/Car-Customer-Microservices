package com.aaronr92.cars.service;

import com.aaronr92.cars.CarDto;
import com.aaronr92.cars.CarResponse;
import com.aaronr92.cars.entity.Car;
import com.aaronr92.cars.entity.CarManufacturer;
import com.aaronr92.cars.repository.CarManufacturerRepository;
import com.aaronr92.cars.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.ignoreCase;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;
    private final CarManufacturerRepository manufacturerRepository;

    public Car addCar(CarDto carDto) {
        CarManufacturer manufacturer = getManufacturer(carDto.getManufacturer());

        Car car = Car.fromCarDto(carDto);
        car.setManufacturer(manufacturer);

        if (carRepository.exists(Example.of(car, ExampleMatcher.matching()
                .withIgnorePaths("id")
                .withIgnorePaths("manufacturer")
                .withMatcher("name", ignoreCase())
                .withMatcher("power", ignoreCase())
                .withMatcher("description", ignoreCase())))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This car already exist");
        }

        log.info("New car added: {}", car);
        manufacturer.addCar(car);
        manufacturerRepository.save(manufacturer);
        return carRepository.save(car);
    }

    public CarResponse getCarById(Long id) {
        return CarResponse.carToCarResponse(findCarById(id));
    }

    public Car findCarById(Long id) {
        Optional<Car> car = carRepository.findById(id);

        log.info("Get {}", car);

        if (car.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Car does not exist");

        return car.get();
    }

    public CarResponse findCarByName(String name) {
        Car car = carRepository.findCarByName(name);

        if (car == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Car does not exist");

        return CarResponse.carToCarResponse(car);
    }

    public Car updateCar(Long id, CarDto carDto) {
        Car car = findCarById(id);

        Car carFromDto = Car.fromCarDto(carDto);

        if (!car.getManufacturer().getName().equals(carDto.getManufacturer())) {
            CarManufacturer manufacturer = getManufacturer(carDto.getManufacturer());
            carFromDto.setManufacturer(manufacturer);
        }

        carFromDto.setId(car.getId());

        return carRepository.save(carFromDto);
    }

    public void deleteCar(Long id) {
        Car car = findCarById(id);

        log.info("Car deleted: {}", car);
        carRepository.deleteById(id);
    }

    private CarManufacturer getManufacturer(String manufacturerName) {
        CarManufacturer manufacturer = manufacturerRepository
                .findCarManufacturerByNameIgnoreCase(manufacturerName);

        if (manufacturer == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This car manufacturer does not exist");

        return manufacturer;
    }
}
