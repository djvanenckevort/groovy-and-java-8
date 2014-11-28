package com.github.freekdb.groovy_and_java_8.movies;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class with movie queries, comparing Java 7 versus Java 8 style.
 *
 * @author <a href="mailto:freekdb@gmail.com">Freek de Bruijn</a>
 */
public class MovieQueries {
    /**
     * Main method.
     *
     * @param arguments unused command-line arguments.
     */
    public static void main(final String[] arguments) {
        new MovieQueries().runQueries();
    }

    /**
     * Compare several queries on a movies list using Java 7 versus Java 8 style.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private void runQueries() {
        final List<Movie> movies = readMoviesAndActors("data/movies-mpaa.txt");
        System.out.println("movies.size(): " + movies.size());
        System.out.println();

        printYearWithMostMovies(movies);
        printMostActiveActor(movies);

        // Most seen actor in one year? Phil Hawn: 24 movies in 1999
    }

    /**
     * Read the movies list from the input file.
     *
     * @param filePath the input file path.
     * @return the movies list.
     */
    private List<Movie> readMoviesAndActors(final String filePath) {
        final List<Movie> movies = new ArrayList<>();
        try {
            final BufferedReader dataReader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = dataReader.readLine()) != null)
                movies.add(new Movie(line));
            dataReader.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return movies;
    }

    /**
     * Print the year in which the most movies were released (and the movie count). Expected answers: 1997 and 700.
     *
     * @param movies the movie list.
     */
    private void printYearWithMostMovies(final List<Movie> movies) {
        // Java 7 style.
        final Map<Integer, Integer> yearToMovieCount = new HashMap<>();
        for (final Movie movie : movies) {
            final int year = movie.getReleaseYear();
            yearToMovieCount.put(year, 1 + (yearToMovieCount.containsKey(year) ? yearToMovieCount.get(year) : 0));
        }
        int maximumMovieCount = 0;
        int maximumMovieYear = 0;
        for (int year : yearToMovieCount.keySet()) {
            final int movieCount = yearToMovieCount.get(year);
            if (movieCount > maximumMovieCount) {
                maximumMovieCount = movieCount;
                maximumMovieYear = year;
            }
        }
        System.out.println("maximumMovieYear: " + maximumMovieYear);
        System.out.println("yearToMovieCount.get(maximumMovieYear): " + yearToMovieCount.get(maximumMovieYear));
        System.out.println();

        // Java 8 style.
        final Map.Entry<Integer, Long> yearAndCountEntry = movies.stream()
            .collect(Collectors.groupingBy(Movie::getReleaseYear, Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .get();
        System.out.println("maximumMovieYear: " + yearAndCountEntry.getKey());
        System.out.println("yearToMovieCount.get(maximumMovieYear): " + yearAndCountEntry.getValue());
        System.out.println();
    }

    /**
     * Print the actor that has worked on the most movies. Expected answer: Frank Welker and 92.
     *
     * @param movies the movie list.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private void printMostActiveActor(final List<Movie> movies) {
        // Java 7 style.
        final Map<Actor, Long> actorToMovieCount = new HashMap<>();
        for (final Movie movie : movies)
            for (final Actor actor : movie.getActors())
                actorToMovieCount.put(actor, 1 + (actorToMovieCount.containsKey(actor) ? actorToMovieCount.get(actor) : 0));
        long maximumMovieCount = 0;
        Actor mostProductiveActor = null;
        for (final Actor actor : actorToMovieCount.keySet())
            if (actorToMovieCount.get(actor) > maximumMovieCount) {
                maximumMovieCount = actorToMovieCount.get(actor);
                mostProductiveActor = actor;
            }
        if (mostProductiveActor != null) {
            System.out.println("Most productive actor: " + mostProductiveActor.getFullName());
            System.out.println("Movie count: " + actorToMovieCount.get(mostProductiveActor));
        } else
            System.out.println("Most productive actor: not found");
        System.out.println();

        // Java 8 style.
    }
}
