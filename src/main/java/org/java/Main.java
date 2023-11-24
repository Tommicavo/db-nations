package org.java;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
	
	private static final String dbUrl = "jdbc:mysql://localhost:3306/db_nations";
	private static final String user = "root";
	private static final String password = "root";
	
	public static void query_1() {	
		try (Connection con = DriverManager.getConnection(dbUrl, user, password)) {
			
			String query = ""
					+ "SELECT\r\n"
					+ "	`countries`.`name` AS NAME,\r\n"
					+ "    `countries`.`country_id` AS ID,\r\n"
					+ "    `regions`.`name` AS REGION,\r\n"
					+ "    `continents`.`name` AS CONTINENT\r\n"
					+ "FROM `countries`\r\n"
					+ "	JOIN `regions`\r\n"
					+ "    	ON `regions`.`region_id` = `countries`.`region_id`\r\n"
					+ "    JOIN `continents`\r\n"
					+ "    	ON `continents`.`continent_id` = `regions`.`continent_id`\r\n"
					+ "ORDER BY `countries`.`name`;";
			
			try (PreparedStatement ps = con.prepareStatement(query)) {
				
				try (ResultSet rs = ps.executeQuery()) {
					
					while (rs.next()) {						
						String countryName = rs.getString(1);
						int countryId = rs.getInt(2);
						String countryRegion = rs.getString(3);
						String countryContinent = rs.getString(4);
						
						System.out.println(
								"Country: " + countryName + ' ' + '[' + countryId + ']' + '\n' +
								"Region: " + countryRegion + '\n' +
								"Continent: " + countryContinent + "\n\n");
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("DB Error: " + e.getMessage());
		}
	}
	
	public static void query_2(Scanner in) {
		List<Integer> searchedIds = new ArrayList<>();

		System.out.println("Search a country:");
		String userCountry = in.nextLine();
		
		try (Connection con = DriverManager.getConnection(dbUrl, user, password)) {
			
			String query = ""
					+ "SELECT\r\n"
					+ "	`countries`.`name` AS NAME,\r\n"
					+ "    `countries`.`country_id` AS ID,\r\n"
					+ "    `regions`.`name` AS REGION,\r\n"
					+ "    `continents`.`name` AS CONTINENT\r\n"
					+ "FROM `countries`\r\n"
					+ "	JOIN `regions`\r\n"
					+ "    	ON `regions`.`region_id` = `countries`.`region_id`\r\n"
					+ "    JOIN `continents`\r\n"
					+ "    	ON `continents`.`continent_id` = `regions`.`continent_id`\r\n"
					+ "WHERE `countries`.`name` LIKE ?\r\n"
					+ "ORDER BY `countries`.`name`;";
			
			try (PreparedStatement ps = con.prepareStatement(query)) {
				
				ps.setString(1, '%' + userCountry + '%');
				
				try (ResultSet rs = ps.executeQuery()) {
					
					while (rs.next()) {							
						String countryName = rs.getString(1);
						int countryId = rs.getInt(2);
						String countryRegion = rs.getString(3);
						String countryContinent = rs.getString(4);
						
						searchedIds.add(countryId);
						
						System.out.println(
								"Country: " + countryName + ' ' + '[' + countryId + ']' + '\n' +
								"Region: " + countryRegion + '\n' +
								"Continent: " + countryContinent + "\n\n");
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("DB Error: " + e.getMessage());
		}

		query_3(searchedIds, in);
	}
	
	public static void query_3(List<Integer> ids, Scanner in) {
		System.out.println("Write an ID to know more about coutries you looked for.");
		System.out.println("Choose from the following: ");
		for (Integer id : ids) {
			System.out.println(id);
		}
		System.out.println("Or type '0' to exit");
		
		int userId = -1;
		while (true) {			
			userId = Integer.valueOf(in.nextLine());
			
			if (userId == 0) {
				in.close();
				return;
			}
			else if (!ids.contains(userId)) {
				System.out.println("The ID: " + userId + " is not from the countries you looked for, try again or exit the program.");
				continue;
			}
			else break;
		}
		
		try (Connection con = DriverManager.getConnection(dbUrl, user, password)) {
			
			String query = ""
					+ "SELECT\r\n"
					+ "    `countries`.`name` AS NAME,\r\n"
					+ "    `countries`.`country_id` AS ID,\r\n"
					+ "    `languages`.`language` AS LANG,\r\n"
					+ "    `country_stats`.`year` AS YEAR,\r\n"
					+ "    `country_stats`.`population` AS POP,\r\n"
					+ "    `country_stats`.`gdp` AS GDP\r\n"
					+ "FROM\r\n"
					+ "    `countries`\r\n"
					+ "JOIN\r\n"
					+ "    `country_languages`ON `country_languages`.`country_id` = `countries`.`country_id`\r\n"
					+ "JOIN\r\n"
					+ "    `languages` ON `languages`.`language_id` = `country_languages`.`language_id`\r\n"
					+ "JOIN\r\n"
					+ "    `country_stats` ON `country_stats`.`country_id` = `countries`.`country_id`\r\n"
					+ "WHERE\r\n"
					+ "    `countries`.`country_id` = ?\r\n"
					+ "    AND `country_stats`.`year` = (\r\n"
					+ "        SELECT MAX(`year`)\r\n"
					+ "        FROM `country_stats`\r\n"
					+ "        WHERE `country_id` = ?\r\n"
					+ "    )\r\n"
					+ "ORDER BY\r\n"
					+ "    `country_stats`.`year` DESC;";
			
			try (PreparedStatement ps = con.prepareStatement(query)) {
				
				ps.setInt(1, userId);
				ps.setInt(2, userId);
				
				List<String> languages = new ArrayList<>();
				
				try (ResultSet rs = ps.executeQuery()) {
					
					boolean firstCycle = true;
					
					String name = null;
					int id = -1;
					String lang = null;
					int year = -1;
					int population = -1;
					long gdp = -1;
					
					while (rs.next()) {

						if (firstCycle) {
							name = rs.getString(1);
							id = rs.getInt(2);
							lang = rs.getString(3);
							year = rs.getInt(4);
							population = rs.getInt(5);
							gdp = rs.getLong(6);
							
							firstCycle = false;
						}
						
						lang = rs.getString(3);
						languages.add(lang);
					}
					System.out.println("Detail for country: " + name + " [" + id + ']');
					System.out.println("Languages:");
					for (String language : languages) {
						System.out.println("- " + language);
					}
					System.out.println("Most recent Stats");
					System.out.println(""
							+ "Year: " + year + '\n'
							+ "Population: " + population + '\n'
							+ "Gdp: " + gdp);
				}
			}
		} catch (SQLException e) {
			System.out.println("DB Error: " + e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		// query_1();
		query_2(in);
		in.close();
	}
}
