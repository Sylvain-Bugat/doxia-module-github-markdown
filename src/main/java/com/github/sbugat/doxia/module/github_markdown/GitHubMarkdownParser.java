package com.github.sbugat.doxia.module.github_markdown;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.maven.doxia.logging.Log;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.sink.Sink;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.MarkdownService;
import org.eclipse.egit.github.core.service.RepositoryService;

/**
 * @plexus.component role="org.apache.maven.doxia.parser.Parser"
 *                   role-hint="markdown"
 * @version $Id$
 * @author Sylvain Bugat
 */
public class GitHubMarkdownParser implements Parser {

	private final GitHubClient gitHubClient;

	private final Repository gitHubProjectRepository;

	private final MarkdownService markdownService;

	private final GitHubMarkdownParserConfiguration gitHubMarkdownParserConfiguration;

	public GitHubMarkdownParser() throws IOException {

		//GitHub client with optional authentication token
		gitHubClient = new GitHubClient();
		gitHubMarkdownParserConfiguration = new GitHubMarkdownParserConfiguration();
		final String authenticationToken = gitHubMarkdownParserConfiguration.getAuthenticationToken();
		if( null != authenticationToken && ! authenticationToken.isEmpty() ) {
			gitHubClient.setOAuth2Token( authenticationToken );
		}

		final String gitHubUser = gitHubMarkdownParserConfiguration.getGitHubUser();
		final String gitHubProjet = gitHubMarkdownParserConfiguration.getGitHubRepository();

		//Get the optional repository configured
		if( null != gitHubUser && ! gitHubUser.isEmpty() && null != gitHubProjet && ! gitHubProjet.isEmpty() ) {

			final RepositoryService repositoryService = new RepositoryService( gitHubClient );
			gitHubProjectRepository = repositoryService.getRepository( gitHubUser, gitHubProjet );
		}
		else {
			gitHubProjectRepository = null;
		}

		markdownService = new MarkdownService( gitHubClient );
	}

	public void parse( final Reader reader, final Sink sink ) throws ParseException {

		final String renderedMarkdownContent;
		try {
			final String readerContent = readContent(reader);

			if( null != gitHubProjectRepository ) {
				final InputStream markdownStream = markdownService.getRepositoryStream( gitHubProjectRepository, readerContent );
				renderedMarkdownContent = readContent( markdownStream );

			}
			else {
				final InputStream markdownStream = markdownService.getStream( readerContent, MarkdownService.MODE_MARKDOWN );
				renderedMarkdownContent = readContent( markdownStream );
			}
		}
		catch( final IOException e ) {
			throw new ParseException( "Error during processing GitHub Markdown", e );
		}

		sink.rawText( renderedMarkdownContent );

		sink.flush();
		sink.close();
	}

	/**
	 * Get the reader content
	 *
	 * @param reader to read
	 * @return content read
	 * @throws IOException
	 */
	private String readContent( final Reader reader ) throws IOException {

		try {
			final StringBuilder stringBuilder = new StringBuilder();
			final char[] buffer = new char[ 4096 ];

			//Read blocks from reader
			int count = reader.read(buffer ) ;
			while ( count > 0 ) {
				stringBuilder.append( buffer, 0, count );
				count = reader.read( buffer ) ;
			}

			return stringBuilder.toString();
		}
		finally {
			reader.close();
		}
	}

	/**
	 * Get the inputStream content
	 *
	 * @param reader to read
	 * @return content read
	 * @throws IOException
	 */
	private String readContent( final InputStream inputStream ) throws IOException {

		try {
			final BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( inputStream, gitHubMarkdownParserConfiguration.getFileEncoding() ) );

			final StringBuilder stringBuilder = new StringBuilder();
			final char[] buffer = new char[ 4096 ];

			//Read blocks from reader
			int count = bufferedReader.read( buffer );
			while( count > 0 ){

				stringBuilder.append( buffer, 0, count );
				count = bufferedReader.read( buffer ) ;
			}

			return stringBuilder.toString();
		}
		finally {
			inputStream.close();
		}
	}

	public int getType() {
		return TXT_TYPE;
	}

	public void enableLogging( final Log log ) {

		//Nothing to log
	}
}
