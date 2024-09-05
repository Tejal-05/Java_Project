create database Ticket;
Use Ticket;

-- Create User table to store user information
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    age INT NOT NULL
    CHECK(AGE>=18)
);
insert into users values(1,"Sona","s","sona@gmail.com",20),(2,"John","j","john@gmail.com",24),
(3,"Tom","t","tom@gmail.com",25),(4,"Bunty","b","bunty@gmail.com",20),(5,"Angel","a","angel@gmail.com",20),
(6,"Lily","l","lily@gmail.com",20);

-- Create Cities table to store available cities
CREATE TABLE cities (
    city_id INT AUTO_INCREMENT PRIMARY KEY,
    city_name VARCHAR(255) NOT NULL
);
INSERT INTO ticket.cities (city_id, city_name)
VALUES (11, 'karad');
INSERT INTO ticket.cities (city_id, city_name)
VALUES (12, 'sangli');
INSERT INTO ticket.cities (city_id, city_name)
VALUES (13, 'Islampur');
INSERT INTO ticket.cities (city_id, city_name)
VALUES (14, 'Kolhapur');

-- Create Theaters table to store theaters associated with cities
CREATE TABLE theaters (
    theater_id INT AUTO_INCREMENT PRIMARY KEY,
    theater_name VARCHAR(255) NOT NULL,
    city_id INT,
    FOREIGN KEY (city_id) REFERENCES cities(city_id)
);
insert into theaters values(21,"mankeshwar",(SELECT city_id FROM cities WHERE city_name = 'Islampur')),
                            (22,"jayhind",(SELECT city_id FROM cities WHERE city_name = 'Islampur')),
                            (23,"shivparvati",(SELECT city_id FROM cities WHERE city_name = 'Islampur'));
INSERT INTO ticket.theaters (theater_id, theater_name,city_id)
VALUES (24, 'INOX',13);
INSERT INTO ticket.theaters (theater_id, theater_name,city_id)
VALUES (25, 'PVR Cinemas',13);
INSERT INTO ticket.theaters (theater_id, theater_name,city_id)
VALUES (26, 'Regal',12);
INSERT INTO ticket.theaters (theater_id, theater_name,city_id)
VALUES (27, 'Galaxy',14);
INSERT INTO ticket.theaters (theater_id, theater_name,city_id)
VALUES (28, 'Asian',14);                            

-- Create TicketInfo table to store available seats, number of tickets required, and cost of tickets
CREATE TABLE ticket_info (
    ticket_id INT AUTO_INCREMENT PRIMARY KEY,
    theater_id INT,
    movie_name VARCHAR(255) NOT NULL,
    available_seats INT NOT NULL,
    reserved_seats INT DEFAULT 0,
    show_time TIME NOT NULL,
    cost_per_ticket DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (theater_id) REFERENCES theaters(theater_id)
);
select * from ticket_info;
-- Create Movies table to store information about movies
CREATE TABLE movies (
    movie_id INT AUTO_INCREMENT PRIMARY KEY,
    movie_name VARCHAR(255) NOT NULL,
    genre VARCHAR(255),
    duration_minutes INT,
	release_date DATE,
    movie_time TIME,
    theater_id INT,
    FOREIGN KEY (theater_id) REFERENCES theaters(theater_id)
);

INSERT INTO ticket.movies(movie_id,movie_name,genre,duration_minutes,release_date, movie_time)
VALUES(33,'Super 30','Inspirational',120,"2023-5-18","12:00:00");
INSERT INTO ticket.movies(movie_id,movie_name,genre,duration_minutes,release_date, movie_time)
VALUES(34,'ABCD','Inspirational',120,"2023-5-1","3:00:00");
INSERT INTO ticket.movies(movie_id,movie_name,genre,duration_minutes,release_date, movie_time)
VALUES(35,'Dangal','Sports',120,"2020-5-31","6:00:00");
INSERT INTO ticket.movies(movie_id,movie_name,genre,duration_minutes,release_date, movie_time)
VALUES(36,'Chennai Express','Comedy',120,"2021-6-7","12:00:00");
INSERT INTO ticket.movies(movie_id,movie_name,genre,duration_minutes,release_date, movie_time)
VALUES(37,'Pathan','War',120,"2023-10-28","3:00:00");
INSERT INTO ticket.movies(movie_id,movie_name,genre,duration_minutes,release_date, movie_time)
VALUES(38,'Avengers','Action',120,"2020-5-23","6:00:00");
select * from users;
select * from cities;
select * from theaters;
select * from movies;
-- Create Tickets table to store information about generated tickets
CREATE TABLE tickets (
    ticket_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255),
    movie_name VARCHAR(255),
    seat_number INT,
    movie_time VARCHAR(255) 
);
select * from tickets;
CREATE TABLE admins (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_username VARCHAR(255) NOT NULL,
    admin_password VARCHAR(255) NOT NULL
);
INSERT INTO admins (admin_username, admin_password) VALUES
    ('admin1', '1'),
    ('admin2', '2');   
CREATE TABLE CombinedCityTheaterMovie AS
SELECT c.city_id, c.city_name, t.theater_id, t.theater_name, m.movie_id, m.movie_name
FROM cities c
LEFT JOIN theaters t ON c.city_id = t.city_id
LEFT JOIN movies m ON t.theater_id = m.theater_id;
