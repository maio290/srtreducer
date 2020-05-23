package de.melancholia.it.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class SRTReducer {
	
	
	public static void printPathList(List<Path> content)
	{
		System.out.println("Found "+content.size()+" files:");
		for(Path entry : content)
		{
			System.out.println(entry.getFileName().toString());
		}
	}
	
	
	public static String advancedTrim(String target)
	{
		target = target.trim();
		if(target.length() > 0)
		{
			if((int) target.charAt(0) == 65279)
			{
				target = target.substring(1);
			}
		}
		
		return target;
	}
	
	public static void reduceFile(Path file, Path dest) throws IOException
	{
		System.out.println("Reducing file: "+file.getFileName());
		List<String> fileContents = Files.readAllLines(file);
		List<String> rawLines = new ArrayList<>();
			
		boolean lastLineWasNumercialOnly = false;
		boolean isInContentArea = false;
		
		final String NUMERICAL_REGEX  =  "^\\d+$";
		final String FROM_TO_REGEX = "\\d{2,}:\\d{2,}:\\d{2,},\\d{2,} --> \\d{2,}:\\d{2,}:\\d{2,},\\d{2,}";
		final String STRIP_REGEX = "\\{.+\\}";
		
		for(String line : fileContents)
		{
			line = advancedTrim(line);
			line = line.replaceAll(STRIP_REGEX, "");
			
			if(!isInContentArea && line.matches(NUMERICAL_REGEX))
			{ 
				lastLineWasNumercialOnly = true;
				continue;
			}
			
			if(!isInContentArea && lastLineWasNumercialOnly && line.matches(FROM_TO_REGEX))
			{
				isInContentArea = true;
				lastLineWasNumercialOnly = false;
				continue;
			}
			
			if(line.isEmpty())
			{
				isInContentArea = false;
				lastLineWasNumercialOnly = false;
			}
			
			if(isInContentArea)
			{
				rawLines.add(line);
			}
			
		}
		
		Files.write(dest, rawLines);	
		System.out.println("File reduced and written to: "+dest.toAbsolutePath());
	}
	
	
	public static void main(String[] args) throws IOException
	{
		System.out.println("SRT Reducer: Removes all non-subtitle information from a given txt or srt file.");
		Scanner scanner = new Scanner(System.in);
		System.out.print("Please enter the absolute folder-path to be converted: ");
		String rawPath = scanner.nextLine();
		
		
		Path resultDir = Paths.get(rawPath).resolve("reduced");
		Files.createDirectories(resultDir);
		
		
		List<Path> contents = Files.walk(Paths.get(rawPath), 1).filter(p -> p.getFileName().toString().endsWith(".srt") || p.getFileName().toString().endsWith(".txt")).collect(Collectors.toList());
		
		System.out.println();
		printPathList(contents);
		
		System.out.println();
		System.out.println("Beginning to reduce files, results will be saved in: "+resultDir.toAbsolutePath().toString());
		
		for(Path file : contents)
		{
			Path dest = resultDir.resolve(file.getFileName());
			reduceFile(file,dest);
		}
		
		System.out.println("Finished reducing files.");
		scanner.close();
		
		
		
		
	}

}
