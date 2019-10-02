package br.com.thiaguten.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.hibernate.jpa.HibernatePersistenceProvider;

/**
 * 
 * @author Thiago Gutenberg Carvalho da Costa
 */
public class PersistenceUnitInfoImpl implements PersistenceUnitInfo {

	private final String persistenceUnitName;
	private final Class<?>[] managedClasses;
	private final String persistenceXMLSchemaVersion = "2.1";
	
	public PersistenceUnitInfoImpl(String persistenceUnitName, Class<?>... managedClasses) {
		this.persistenceUnitName = persistenceUnitName;
		this.managedClasses = managedClasses;
	}

	@Override
	public String getPersistenceUnitName() {
		return persistenceUnitName;
	}

	@Override
	public String getPersistenceProviderClassName() {
		return HibernatePersistenceProvider.class.getName();
	}

	@Override
	public PersistenceUnitTransactionType getTransactionType() {
		return PersistenceUnitTransactionType.RESOURCE_LOCAL;
	}

	@Override
	public DataSource getJtaDataSource() {
		return null;
	}

	@Override
	public DataSource getNonJtaDataSource() {
		return null;
	}

	@Override
	public List<String> getMappingFileNames() {
		return Collections.emptyList();
	}

	@Override
	public List<URL> getJarFileUrls() {
//		return Collections.emptyList();
		try {
//			return Collections.list(ApplicationPersistenceProvider.getDefaultClassLoader().getResources(""))
			return Collections.list(this.getClass().getClassLoader().getResources(""))
					.stream()
					.filter(url -> "jar".equalsIgnoreCase(url.getProtocol()))
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public URL getPersistenceUnitRootUrl() {
		return PersistenceUnitInfoImpl.getDefaultClassLoader().getResource("");
	}

	@Override
	public List<String> getManagedClassNames() {
		return managedClasses == null 
				? Collections.emptyList() 
				: Stream.of(managedClasses).map(Class::getName).collect(Collectors.toList());
	}

	@Override
	public boolean excludeUnlistedClasses() {
		return managedClasses != null && managedClasses.length > 0;
	}

	@Override
	public SharedCacheMode getSharedCacheMode() {
		return SharedCacheMode.UNSPECIFIED;
	}

	@Override
	public ValidationMode getValidationMode() {
		return ValidationMode.AUTO;
	}

	@Override
	public Properties getProperties() {
		return new Properties();
	}

	@Override
	public String getPersistenceXMLSchemaVersion() {
		return persistenceXMLSchemaVersion;
	}

	@Override
	public ClassLoader getClassLoader() {
		return null;
//		return ApplicationPersistenceProvider.getDefaultClassLoader();
	}

	@Override
	public void addTransformer(ClassTransformer transformer) {
		
	}

	@Override
	public ClassLoader getNewTempClassLoader() {
		return null;
	}
	
	/**
	 * Return the default ClassLoader to use: typically the thread context
	 * ClassLoader, if available; the ClassLoader that loaded the ClassUtils class
	 * will be used as fallback.
	 * <p>
	 * Call this method if you intend to use the thread context ClassLoader in a
	 * scenario where you clearly prefer a non-null ClassLoader reference: for
	 * example, for class path resource loading (but not necessarily for
	 * {@code Class.forName}, which accepts a {@code null} ClassLoader reference as
	 * well).
	 * 
	 * @return the default ClassLoader (only {@code null} if even the system
	 *         ClassLoader isn't accessible)
	 * @see Thread#getContextClassLoader()
	 * @see ClassLoader#getSystemClassLoader()
	 */
	public static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		} catch (Throwable ex) {
			// Cannot access thread context ClassLoader - falling back...
		}
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			cl = PersistenceHelper.class.getClassLoader();
			if (cl == null) {
				// getClassLoader() returning null indicates the bootstrap ClassLoader
				try {
					cl = ClassLoader.getSystemClassLoader();
				} catch (Throwable ex) {
					// Cannot access system ClassLoader - oh well, maybe the caller can live with
					// null...
				}
			}
		}
		return cl;
	}
}
