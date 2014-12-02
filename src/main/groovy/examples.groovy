#!/usr/bin/env groovy

// where is the data located
def moviesFileLocation = '../../../data/movies-mpaa.txt' 
def moviesFile = new File(moviesFileLocation)

// store data file on local disk (only if you have internet :)
// def moviesFileURL = 'http://introcs.cs.princeton.edu/java/data/movies-mpaa.txt'
// moviesFile.delete()
// moviesFile << new URL(moviesFileURL).text

def movies = []
def fullYears = true
 
moviesFile.eachLine { line ->

	def elements = line.split('/') // get elements

	def movie = [:] // line == movie
	movie['title']	= ("${elements[0]}".split('\\(')[0]).trim() // first element, without year part
	movie['year']	= ((elements[0] - movie['title'])[2..-2]).trim() // first element, minus title, trim first and last ()
	movie['actors']	= elements[1..-1].collect { it.split(',').reverse().join(" ").trim() } // from remaining elements, collect elements after split by comma, in reverse order to have FN+LN and join/concat them with space 

	if (fullYears){ movie['year'] = movie['year'].split(',')[0] } // remove I or II

	// add to movies list
	movies << movie
}

// some handy lists
def years 	= movies.collect { it['year'] }.unique().sort()
def yearMax	= years.max() as int
def yearMin	= years.min() as int

/*************************************************************/

print "Number of movie titles found: "
println movies.count { it['title'] }


/*************************************************************/

print "Number of unique movie titles found: "
println movies.collect { it['title'] }.unique().size()


/*************************************************************/

println "Number of movies by year: "
years.each { year -> println year + "\t" + movies.count { it['year'] == year } }


/*************************************************************/

println "Year(s) without movies: "
println (((yearMin..yearMax).collect { it as String }) - years)


/*************************************************************/

print "The most active actor of all time is "
actors = [:]
movies.each { movie ->
	movie['actors'].each { actor ->
		(!actors[actor]) ? actors[actor] = 1 : actors[actor]++
	}
}
def mostActiveActor = actors.sort { it.value }.collect { it }.pop()
println mostActiveActor.key + " with " + mostActiveActor.value + " movies."


/*************************************************************/

print "Most active actor by year: "
years.each { year ->
	println ""
	print year + "\t"
	actors = [:] // reset actors
	movies.findAll { it['year'] == year }.each { movie ->
		movie['actors'].each { actor ->
			(!actors[actor]) ? actors[actor] = 1 : actors[actor]++
		}
	}
	def mostActiveActorByYear = actors.sort { it.value }.collect { it }.pop()
	print mostActiveActorByYear.key + "\t"
	print mostActiveActorByYear.value + "\t"
	
	def actorsWithSameActivity = ((actors.findAll { it.value == mostActiveActorByYear.value }.collect{ it.key }) - mostActiveActorByYear.key).unique()
	if (actorsWithSameActivity){
		print "(like: " + actorsWithSameActivity.join(', ') + ")"
	}		
}

/*************************************************************/	
println ""