/**
 * 
 */
package org.allGraphQLCases.server.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Resource;

import org.allGraphQLCases.server.AllFieldCases;
import org.allGraphQLCases.server.AllFieldCasesInput;
import org.allGraphQLCases.server.AllFieldCasesWithIdSubtype;
import org.allGraphQLCases.server.AllFieldCasesWithoutIdSubtype;
import org.allGraphQLCases.server.FieldParameterInput;
import org.allGraphQLCases.server.Human;
import org.allGraphQLCases.server.impl.DataFetchersDelegateAllFieldCasesWithIdSubtypeImpl.KeyContext;
import org.allGraphQLCases.server.util.DataFetchersDelegateAllFieldCases;
import org.dataloader.BatchLoaderEnvironment;
import org.dataloader.DataLoader;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetchingEnvironment;

/**
 * @author etienne-sf
 *
 */
@Component
public class DataFetchersDelegateAllFieldCasesImpl implements DataFetchersDelegateAllFieldCases {

	@Resource
	DataGenerator generator;

	@Override
	public List<String> comments(DataFetchingEnvironment dataFetchingEnvironment, AllFieldCases source) {
		// If this attribute was set, let's keep its value
		if (source.getComments() != null)
			// The field has already been filled (probably from incoming argument). We'll keep it
			return source.getComments();
		else
			return generator.generateInstanceList(String.class, 10);
	}

	@Override
	public List<Boolean> booleans(DataFetchingEnvironment dataFetchingEnvironment, AllFieldCases source) {
		// If this attribute was set, let's keep its value
		if (source.getBooleans() != null)
			// The field has already been filled (probably from incoming argument). We'll keep it
			return source.getBooleans();
		else
			return generator.generateInstanceList(Boolean.class, 10);
	}

	@Override
	public List<String> aliases(DataFetchingEnvironment dataFetchingEnvironment, AllFieldCases source) {
		return generator.generateInstanceList(String.class, 10);
	}

	@Override
	public List<String> planets(DataFetchingEnvironment dataFetchingEnvironment, AllFieldCases source) {
		return generator.generateInstanceList(String.class, 10);
	}

	@Override
	public List<Human> friends(DataFetchingEnvironment dataFetchingEnvironment, AllFieldCases source) {
		return generator.generateInstanceList(Human.class, 10);
	}

	@Override
	public List<List<Double>> matrix(DataFetchingEnvironment dataFetchingEnvironment, AllFieldCases origin) {
		// When the request is "withListOfList", the matrix field is field from the input parameter.
		// So, if this field is non null, we let its value. Otherwise, we provide one.
		if (origin.getMatrix() != null) {
			// The field has already been filled (probably from incoming argument). We'll keep it
			return origin.getMatrix();
		} else {
			List<List<Double>> list = new ArrayList<>();
			for (int i = 0; i < 2; i += 1) {
				List<Double> sublist = new ArrayList<>();
				sublist.add(DataGenerator.RANDOM.nextDouble());
				sublist.add(DataGenerator.RANDOM.nextDouble());
				sublist.add(DataGenerator.RANDOM.nextDouble());
				list.add(sublist);
			}
			return list;
		}
	}

	@Override
	public List<AllFieldCases> batchLoader(List<UUID> keys, BatchLoaderEnvironment environment) {
		return generator.generateInstanceList(AllFieldCases.class, keys.size());
	}

	@Override
	public CompletableFuture<AllFieldCasesWithIdSubtype> oneWithIdSubType(
			DataFetchingEnvironment dataFetchingEnvironment, DataLoader<UUID, AllFieldCasesWithIdSubtype> dataLoader,
			AllFieldCases source, Boolean uppercase) {
		KeyContext kc = new KeyContext();
		kc.uppercase = uppercase;
		return dataLoader.load(UUID.randomUUID(), kc);
	}

	@Override
	public AllFieldCasesWithIdSubtype oneWithIdSubType(DataFetchingEnvironment dataFetchingEnvironment,
			AllFieldCases origin, Boolean uppercase) {
		return generator.generateInstance(AllFieldCasesWithIdSubtype.class);
	}

	@Override
	public CompletableFuture<List<AllFieldCasesWithIdSubtype>> listWithIdSubTypes(
			DataFetchingEnvironment dataFetchingEnvironment, DataLoader<UUID, AllFieldCasesWithIdSubtype> dataLoader,
			AllFieldCases origin, Long nbItems, Date date, List<Date> dates, Boolean uppercaseName,
			String textToAppendToTheForname) {

		List<UUID> uuids = generator.generateInstanceList(UUID.class, nbItems.intValue());

		// We store the parameter that'll allow the datafetcher to return a AllFieldCasesWithIdSubtype that respects
		// what the GraphQL request expects
		List<Object> keyContexts = new ArrayList<>();
		KeyContext kc = new KeyContext();
		kc.uppercase = uppercaseName;
		kc.textToAppendToTheForname = textToAppendToTheForname;
		for (int i = 0; i < uuids.size(); i += 1) {
			keyContexts.add(kc);
		}

		return dataLoader.loadMany(uuids, keyContexts);
	}

	@Override
	public List<AllFieldCasesWithIdSubtype> listWithIdSubTypes(DataFetchingEnvironment dataFetchingEnvironment,
			AllFieldCases source, Long nbItems, Date date, List<Date> dates, Boolean uppercaseName,
			String textToAppendToTheForname) {
		List<AllFieldCasesWithIdSubtype> list = generator.generateInstanceList(AllFieldCasesWithIdSubtype.class, 3);

		for (AllFieldCasesWithIdSubtype item : list) {
			if (uppercaseName != null && uppercaseName) {
				item.setName(item.getName().toUpperCase());
			}
			item.setName(item.getName() + textToAppendToTheForname);
		}

		return list;
	}

	@Override
	public AllFieldCasesWithoutIdSubtype oneWithoutIdSubType(DataFetchingEnvironment dataFetchingEnvironment,
			AllFieldCases source, FieldParameterInput input) {
		if (source.getOneWithoutIdSubType() != null) {
			// The field has already been filled (probably from incoming argument). We'll keep it
			return source.getOneWithoutIdSubType();
		} else {
			AllFieldCasesWithoutIdSubtype ret = generator.generateInstance(AllFieldCasesWithoutIdSubtype.class);

			if (input != null && input.getUppercase() != null && input.getUppercase()) {
				if (ret.getName() != null) {
					ret.setName(ret.getName().toUpperCase());
				}
			}

			return ret;

		}
	}

	@Override
	public List<AllFieldCasesWithoutIdSubtype> listWithoutIdSubTypes(DataFetchingEnvironment dataFetchingEnvironment,
			AllFieldCases source, Long nbItems, FieldParameterInput input, String textToAppendToTheForname) {
		if (source.getListWithoutIdSubTypes() != null) {
			// The field has already been filled (probably from incoming argument). We'll keep it
			return source.getListWithoutIdSubTypes();
		} else {
			List<AllFieldCasesWithoutIdSubtype> list = generator
					.generateInstanceList(AllFieldCasesWithoutIdSubtype.class, nbItems.intValue());

			for (AllFieldCasesWithoutIdSubtype item : list) {
				if (input != null && input.getUppercase() != null && input.getUppercase()) {
					item.setName(item.getName().toUpperCase());
				}
				item.setName(item.getName() + textToAppendToTheForname);
			}

			return list;
		}
	}

	@Override
	public List<Date> dates(DataFetchingEnvironment dataFetchingEnvironment, AllFieldCases source) {
		// If this attribute was set, let's keep its value
		if (source.getDates() != null)
			return source.getDates();
		else
			return generator.generateInstanceList(Date.class, 5);
	}

	@Override
	public List<AllFieldCasesWithoutIdSubtype> issue65(DataFetchingEnvironment dataFetchingEnvironment,
			AllFieldCases origin, List<FieldParameterInput> inputs) {

		List<AllFieldCasesWithoutIdSubtype> ret = generator.generateInstanceList(AllFieldCasesWithoutIdSubtype.class,
				inputs.size());

		// Let's put in uppercase the name, for items in the return list that match the inputs that have uppercase set
		// to true
		for (int i = 0; i < inputs.size(); i += 1) {
			AllFieldCasesWithoutIdSubtype item = ret.get(i);
			if (inputs.get(i).getUppercase()) {
				item.setName(item.getName().toUpperCase());
			}
		}
		return ret;
	}

	@Override
	public CompletableFuture<AllFieldCases> issue66(DataFetchingEnvironment dataFetchingEnvironment,
			DataLoader<UUID, AllFieldCases> dataLoader, AllFieldCases origin, List<AllFieldCasesInput> input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AllFieldCases issue66(DataFetchingEnvironment dataFetchingEnvironment, AllFieldCases origin,
			List<AllFieldCasesInput> input) {
		// TODO Auto-generated method stub
		return null;
	}

}
