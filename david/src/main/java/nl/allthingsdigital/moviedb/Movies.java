/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.allthingsdigital.moviedb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author David van Enckevort <david@allthingsdigital.nl>
 */
public class Movies {
    private static final String CLINT = "Eastwood, Clint";

    public static void main(String[] args) throws IOException {

        Movies movies = new Movies();
        for (String file : args) {
            movies.run(file);
        }
    }

    private void run(String file) throws IOException {
        Stream<String> lines = Files.lines(Paths.get(file));

        ActorConsumer actor = new ActorConsumer();
        MovieConsumer titles = new MovieConsumer();
        YearConsumer year = new YearConsumer();

        lines.peek(titles).peek(year).forEach(actor);

        System.out.println(actor.report());

        List<String> moviesForClint = actor.getMoviesForActor(CLINT);
        System.out.println(String.format("There are %d movies featuring %s", moviesForClint.size(), CLINT));
        System.out.println(String.format("Movies featuring %s: %s", CLINT,
                moviesForClint.stream().collect(Collectors.joining("\", \"", "\"", "\""))));

        System.out.println(titles.report());
        System.out.println(year.report());

    }

    private abstract class AbstractMovieDBConsumer implements Consumer<String> {
        abstract String report();

        protected String getTitle(String line) {
            return stripYear(line.split("/")[0]);
        }

        protected String stripYear(String line) {
            int lastIndexOf = line.lastIndexOf('(');
            if (line.lastIndexOf(')') > lastIndexOf) {
                String title = line.substring(0, lastIndexOf).trim();
                return title;
            }
            return line;
        }

        protected Integer getYear(final String line) {
            String title = line.split("/")[0];
            int start = title.lastIndexOf('(');
            int end = title.lastIndexOf(')');
            if (end > start) {
                String year = title.substring(start + 1, end + 1).trim();
                if (year.length() < 4) {
                    System.out.println(title);
                    return -1;
                }
                return Integer.parseInt(year.substring(0, 4));
            }
            return -1;
        }
    }

    private class ActorConsumer extends AbstractMovieDBConsumer {

        Map<String,List<String>> actors = new ConcurrentHashMap<>();

        @Override
        public void accept(String t) {
            int start = t.indexOf('/');
            List<String> asList = Arrays.asList(t.substring(start).split("/"));
            String movie = getTitle(t);
            asList.stream().forEach(A -> {
                actors.merge(A, getMovieList(movie),
                    (L1, L2) -> { L1.addAll(L2); return L1; } );
            });
        }

        private List<String> getMovieList(String movie) {
            List<String> movies = new ArrayList<>();
            movies.add(movie);
            return movies;
        }

        public List<String> getMoviesForActor(final String actor) {
            return actors.get(actor);
        }
        @Override
        public String report() {
            StringBuilder report = new StringBuilder();
            report.append(String.format("There are %d unique actors in the database", actors.size()));
            return report.toString();
        }

    }

    private class MovieConsumer extends AbstractMovieDBConsumer {

        int count = 0;
        Set<String> unique = new HashSet<>();

        @Override
        public void accept(String t) {
            count++;
            unique.add(getTitle(t));
        }

        @Override
        public String report() {
            return String.format("There are %d movies in the database, with %d unique titles", count, unique.size());
        }
    }

    private class YearConsumer extends AbstractMovieDBConsumer {
        Map<Integer,Integer> map = new ConcurrentHashMap<>();

        @Override
        public void accept(String t) {
            map.merge(getYear(t), 1, (A, B) -> {return A + B;});
        }

        @Override
        public String report() {
            Map<Integer, Integer> wc = new HashMap<>(map);
            final StringBuilder report = new StringBuilder();
            report.append(String.format("Movies without a year: %d\n", wc.get(-1) == null ? 0 : wc.get(-1)));
            wc.remove(-1);
            IntSummaryStatistics yearSummary = wc.keySet().stream().mapToInt(I -> I).summaryStatistics();
            report.append(String.format("First year in movie database: %d\n", yearSummary.getMin()));
            report.append(String.format("Last year in movie database: %d\n", yearSummary.getMax()));
            List<Integer> years = IntStream.rangeClosed(yearSummary.getMin(), yearSummary.getMax()).boxed().collect(Collectors.toList());
            years.removeAll(wc.keySet());
            String withoutMovies = years.stream().map(I -> { return String.valueOf(I); }).collect(Collectors.joining(", "));
            report.append(String.format("Years without movies: %s\n", withoutMovies));
            IntSummaryStatistics movieSummary = wc.values().stream().collect(Collectors.summarizingInt(I -> I));
            report.append(String.format("Most movies in a year: %d\n", movieSummary.getMax()));
            report.append(String.format("Least movies in a year: %d\n", movieSummary.getMin()));
            report.append(String.format("Average movies in a year: %01.2f\n", movieSummary.getAverage()));
            BestYear bestYear = new BestYear();
            wc.forEach(bestYear);
            report.append(String.format("Best year was %d with %d movies\n",
                    bestYear.getBestYear(), bestYear.getMaxMovies()));
            return report.toString();
        }
        private class BestYear implements BiConsumer<Integer, Integer> {
            private int maxMovies = 0;
            private int bestYear = 0;
            @Override
            public void accept(Integer year, Integer movies) {
                if (movies > maxMovies) {
                    bestYear = year;
                    maxMovies = movies;
                }
            }
            public int getBestYear() {
                return bestYear;
            }
            public int getMaxMovies() {
                return maxMovies;
            }
        }
    }
}
