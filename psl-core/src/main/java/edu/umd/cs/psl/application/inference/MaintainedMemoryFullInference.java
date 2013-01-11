/*
 * This file is part of the PSL software.
 * Copyright 2011 University of Maryland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.umd.cs.psl.application.inference;

import com.google.common.collect.ImmutableSet;

import edu.umd.cs.psl.application.groundkernelstore.GroundKernelStore;
import edu.umd.cs.psl.application.groundkernelstore.MemoryGroundKernelStore;
import edu.umd.cs.psl.application.util.Grounding;
import edu.umd.cs.psl.config.ConfigBundle;
import edu.umd.cs.psl.config.ConfigManager;
import edu.umd.cs.psl.config.EmptyBundle;
<<<<<<< HEAD
=======
import edu.umd.cs.psl.config.Factory;
>>>>>>> develop
import edu.umd.cs.psl.database.Database;
import edu.umd.cs.psl.evaluation.process.RunningProcess;
import edu.umd.cs.psl.evaluation.process.local.LocalProcessMonitor;
import edu.umd.cs.psl.evaluation.result.FullInferenceResult;
import edu.umd.cs.psl.evaluation.result.memory.MemoryFullInferenceResult;
import edu.umd.cs.psl.model.Model;
import edu.umd.cs.psl.model.ModelEvent;
import edu.umd.cs.psl.model.atom.Atom;
import edu.umd.cs.psl.model.atom.AtomManager;
import edu.umd.cs.psl.model.atom.AtomStatus;
import edu.umd.cs.psl.model.atom.memory.MemoryAtomManager;
import edu.umd.cs.psl.model.kernel.GroundCompatibilityKernel;
import edu.umd.cs.psl.model.kernel.GroundKernel;
import edu.umd.cs.psl.reasoner.Reasoner;
import edu.umd.cs.psl.reasoner.ReasonerFactory;
import edu.umd.cs.psl.reasoner.admm.ADMMReasonerFactory;

public class MaintainedMemoryFullInference implements FullInference {
	
	private static final Logger log = LoggerFactory.getLogger(MaintainedMemoryFullInference.class);
	
	/**
	 * Prefix of property keys used by this class.
	 * 
	 * @see ConfigManager
	 */
	public static final String CONFIG_PREFIX = "inference";
	
	/**
	 * Key for {@link Factory} or String property.
	 * 
	 * Should be set to a {@link ReasonerFactory} or the fully qualified
	 * name of one. Will be used to instantiate a {@link Reasoner}.
	 */
	public static final String REASONER_KEY = CONFIG_PREFIX + ".reasoner";
	/**
	 * Default value for REASONER_KEY.
	 * 
	 * Value is instance of {@link ADMMReasonerFactory}. 
	 */
	public static final ReasonerFactory REASONER_DEFAULT = new ADMMReasonerFactory();

	private final Database database;
	private final AtomManager atomManager;
	private final Reasoner reasoner;
	private final GroundKernelStore groundKernels;
	
	private boolean hasChanged;
	private boolean isInitialized;
	
	public MaintainedMemoryFullInference(Model m, Database db, ConfigBundle config)
			throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		model = m;
		database = db;
		store = new MemoryAtomStore(database);
		dbProxy = new DatabaseAtomStoreQuery(store);
		groundkernels = new MemoryGroundKernelStore();
		atomEvents = new MemoryAtomEventFramework(m,this,store);
		database.registerDatabaseEventObserver(atomEvents);
		reasoner = ((ReasonerFactory) config.getFactory(REASONER_KEY, REASONER_DEFAULT)).getReasoner(atomEvents, config);
		model.registerModelObserver(this);
		// TODO: Move to AtomManager implementations?
		model.registerModelObserver(atomManager);
		
		hasChanged=false;
		isInitialized=false;
	}
	
	public MaintainedMemoryFullInference(Model m, Database db)
			throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		this(m,db, new EmptyBundle());
	}
	
	//####### GroundKernel Handling #####
	
	@Override
	public void addGroundKernel(GroundKernel e) {
		groundKernels.addGroundKernel(e);
		reasoner.addGroundKernel(e);
		hasChanged=true;
	}
	
	@Override
	public void changedGroundKernel(GroundKernel e) {
		groundKernels.changedGroundKernel(e);
		reasoner.updateGroundKernel(e);
		hasChanged=true;
	}
	
	@Override
	public void removeGroundKernel(GroundKernel e) {
		groundKernels.removeGroundKernel(e);
		reasoner.removeGroundKernel(e);
		hasChanged=true;
	}
	
	@Override
	public boolean containsGroundKernel(GroundKernel e) {
		return groundKernels.containsGroundKernel(e);
	}
	
	@Override
	public GroundKernel getGroundKernel(GroundKernel e) {
		return groundKernels.getGroundKernel(e);
	}
	
	@Override
	public Iterable<GroundKernel> getGroundKernel() {
		return groundKernels.getGroundKernels();
	}

	@Override
	public Iterable<GroundCompatibilityKernel> getCompatibilityKernels() {
		return groundKernels.getCompatibilityKernels();
	}

	@Override
	public void notifyModelEvent(ModelEvent event) {
		switch(event) {
		case KernelAdded:
			throw new UnsupportedOperationException();
		case KernelRemoved:
			throw new UnsupportedOperationException();
		case KernelParametersModified:
			for (GroundKernel e : groundKernels.getGroundKernels(event.getKernel())) {
				if (e.updateParameters()) {
					reasoner.updateGroundKernel(e);
				}
			}
			hasChanged=true;
			break;
		default: throw new IllegalArgumentException("Unrecognized model event type: " + event);
		}
		
	}
	
	//####### Inference #####
	
	@Override
	public void initialize() {
		if (!isInitialized) {
			Grounding.groundAll(model, this);
			while (atomManager.checkToActivate() > 0)
				atomManager.workOffJobQueue();
			isInitialized=true;
		}
	}
	
	@Override
	public FullInferenceResult runInference() {
		RunningProcess proc = LocalProcessMonitor.get().startProcess();
		initialize();
		if (hasChanged) {
			reasoner.mapInference();
			/* Updates truth values */
			
			/* Iterates over all random variables that have a dimension in the optimization */ 
			for (Atom atom : atomManager.getAtoms(ImmutableSet.of(AtomStatus.ActiveRV, AtomStatus.ConsideredRV))) {
				if (atom.getValue() > 0.0) {
					atomManager.persist(atom);
				}
			}
			hasChanged=false;
		}
		proc.terminate();
		return new MemoryFullInferenceResult(proc,groundkernels.getTotalIncompatibility(reasoner.getDistributionType()),
						store.getNumAtoms(ImmutableSet.of(AtomStatus.ConsideredCertainty,AtomStatus.ActiveRV)),
						groundkernels.size());

	}
	
	public double getTotalIncompatibility() {
		return groundkernels.getTotalIncompatibility(reasoner.getDistributionType());
	}
	
	//####### Interface to other components #####
	
	@Override
	public AtomManager getAtomManager() {
		return atomManager;
	}

	@Override
	public void close() {
		model.unregisterModelObserver(this);
		model.unregisterModelObserver(atomManager);
		reasoner.close();
	}



	
}
