package basic_hierarchy.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import basic_hierarchy.common.Constants;
import basic_hierarchy.common.HierarchyFiller;
import basic_hierarchy.implementation.BasicHierarchy;
import basic_hierarchy.implementation.BasicInstance;
import basic_hierarchy.implementation.BasicNode;
import basic_hierarchy.interfaces.DataReader;
import basic_hierarchy.interfaces.Hierarchy;
import basic_hierarchy.interfaces.Node;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class GeneratedARFFReader implements DataReader {

	@Override
	public Hierarchy load(String filePath, boolean withClassAttribute, boolean fillBreathGaps) {
		File inputFile = new File(filePath);
		if(!inputFile.exists() && inputFile.isDirectory())
		{
			System.err.println("Cannot access to file: " + filePath + ". Does it exist and is it a "
					+ "weka ARFF file?\n");
			System.exit(1);
		}
		DataSource source = null;
		Instances data = null;
		try {
			source = new DataSource(inputFile.getAbsolutePath());
			data = source.getDataSet();			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
		data.setClassIndex(Constants.INDEX_OF_ASSIGN_CLASS_IN_WEKA_INSTANCE);
		int assignClassIndex = data.classIndex();
		
		System.out.println("Assign class attribute name is: " + data.classAttribute().name()
				+ "\nNumber of classes: " + data.numClasses());
		
		BasicNode root = null;
		ArrayList<BasicNode> nodes = new ArrayList<BasicNode>();
		int rootIndexInNodes = -1;
		int numberOfInstances = 0;		
		HashMap<String, Integer> eachClassAndItsCount = new HashMap<String, Integer>();
		
		int numberOfDimensions = data.numAttributes() - 1;//minus assign class attribute
		if(withClassAttribute)
		{
			numberOfDimensions -= 1;
		}
		
		
		for(int i = 0; i < data.numInstances(); i++)
		{
			weka.core.Instance inst = data.instance(i);
			
			String classAttrib = null;
			if(withClassAttribute)
			{
				classAttrib = inst.stringValue(Constants.INDEX_OF_GROUND_TRUTH_IN_WEKA_INSTANCE);
				if(eachClassAndItsCount.containsKey(classAttrib))
				{
					eachClassAndItsCount.put(classAttrib, eachClassAndItsCount.get(classAttrib) + 1);
				}
				else
				{
					eachClassAndItsCount.put(classAttrib, 1);
				}
			}
			
			//assuming that node's instances are grouped in input file
			//REFACTOR: below could the binary-search be utilised with sorting by ID-comparator
			
			String assignClass = inst.stringValue(assignClassIndex);
			
			double[] instData = new double[numberOfDimensions];
			int instDataIndex = 0;
			for(int j = 0; j < inst.numAttributes(); j++)
			{
				if(j != Constants.INDEX_OF_ASSIGN_CLASS_IN_WEKA_INSTANCE
						&& j != Constants.INDEX_OF_GROUND_TRUTH_IN_WEKA_INSTANCE)
				{
					instData[instDataIndex] = inst.value(j);
					instDataIndex++;
				}
			}
			
			boolean nodeExist = false;
			int nodeIndex = -1;
			for(int nodeIndexIter = 0; nodeIndexIter < nodes.size() && !nodeExist; nodeIndexIter++)
			{
				if(nodes.get(nodeIndexIter).getId().equalsIgnoreCase(assignClass))
				{
					nodeExist = true;
					nodeIndex = nodeIndexIter;
				}
			}
			if(nodeExist)
			{
				nodes.get(nodeIndex).addInstance(new BasicInstance(nodes.get(nodeIndex).getId(), instData, classAttrib));
				numberOfInstances++;
			}
			else
			{
				BasicNode nodeToAdd = new BasicNode(assignClass, null, new LinkedList<Node>(),
						new LinkedList<basic_hierarchy.interfaces.Instance>());
				nodeToAdd.addInstance(new BasicInstance(nodeToAdd.getId(), instData, classAttrib));
				numberOfInstances++;
				nodes.add(nodeToAdd);
				if(root == null && assignClass.equalsIgnoreCase(Constants.ROOT_ID))
				{
					root = nodes.get(nodes.size()-1);
					rootIndexInNodes = nodes.size()-1;
				}
			}
		}
		
		LinkedList<Node> allNodes = HierarchyFiller.addMissingEmptyNodes(root, nodes, rootIndexInNodes, fillBreathGaps);
		return new BasicHierarchy(root, allNodes, eachClassAndItsCount, numberOfInstances);
	}

}