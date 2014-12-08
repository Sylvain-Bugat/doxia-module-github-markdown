package com.github.sbugat.doxia.module.github_markdown;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class GitHubMarkdownParserConfiguration {

	private static final String GITHUB_USER = "github.user";
	private static final String GITHUB_REPOSITORY = "github.repository";
	private static final String AUTHENTICATION_TOKEN = "authentication.token";
	private static final String FILE_ENCODING_PROPERTY = "file.encoding";
	private static final String FILE_ENCODING_PROPERTY_DEFAULT_VALUE = StandardCharsets.UTF_8.name();

	private static final String GITHUB_MARKDOWN_PARSER_PROPERTIES_FILE = "doxia-github-markdown.properties";

	private String gitHubUser;

	private String gitHubRepository;

	private String authenticationToken;

	private String fileEncoding = FILE_ENCODING_PROPERTY_DEFAULT_VALUE;

	public GitHubMarkdownParserConfiguration() {

		//Configuration loading
		final Path propertyFile = Paths.get( GITHUB_MARKDOWN_PARSER_PROPERTIES_FILE );
		if( ! Files.exists( propertyFile ) ){

			return;
		}

		//Load the configuration file and extract properties
		final Properties properties = new Properties();
		Reader propertyFileReader = null;
		try {
			propertyFileReader = Files.newBufferedReader( propertyFile, StandardCharsets.UTF_8 );
			properties.load( propertyFileReader );
		}
		catch( final IOException e ){
			return;
		}
		finally {
			if( null != propertyFileReader ){
				try {
					propertyFileReader.close();
				}
				catch( final IOException e ) {
					//Give up
					return;
				}
			}
		}

		gitHubUser = properties.getProperty( GITHUB_USER );
		gitHubRepository = properties.getProperty( GITHUB_REPOSITORY );

		authenticationToken = properties.getProperty( AUTHENTICATION_TOKEN );
		fileEncoding = getOptionnalProperty( properties, FILE_ENCODING_PROPERTY, FILE_ENCODING_PROPERTY_DEFAULT_VALUE );
	}

	private static String getOptionnalProperty( final Properties properties, final String propertyName, final String propertyDefaultValue ) {

		final String propertyValue = properties.getProperty( propertyName, propertyDefaultValue );

		if( null == propertyValue || propertyValue.isEmpty() ) {
			return propertyDefaultValue;
		}

		return propertyValue;
	}

	public String getGitHubUser() {
		return gitHubUser;
	}

	public String getGitHubRepository() {
		return gitHubRepository;
	}

	public String getAuthenticationToken() {
		return authenticationToken;
	}

	public String getFileEncoding() {
		return fileEncoding;
	}

}
