package gr.aueb.CIPTIMEFL.graphs;

import static org.neo4j.helpers.collection.IteratorUtil.firstOrNull;
import static org.neo4j.kernel.StandardExpander.toPathExpander;
import static org.neo4j.kernel.Traversal.traversal;

import java.util.Iterator;

import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphalgo.impl.path.Dijkstra;
import org.neo4j.graphalgo.impl.util.BestFirstSelectorFactory;
import org.neo4j.graphalgo.impl.util.StopAfterWeightIterator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.RelationshipExpander;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.InitialBranchState;
import org.neo4j.graphdb.traversal.TraversalBranch;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.TraversalMetadata;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Uniqueness;

public class MyDijkstra implements PathFinder<WeightedPath>{
		
		private static final TraversalDescription TRAVERSAL = traversal().uniqueness( Uniqueness.NONE )
				.evaluator( new Evaluator()
				 {
					@Override
					public Evaluation evaluate( final Path path )
					{	// Define length of path to search for. Here is <= than a depth of 5 and >= than a depth of 3 nodes deep.
						// Also, check if first node is an initiating node.
						Node node = path.startNode();
						boolean isInit = (boolean) node.getProperty("init");
						if (path.length() <= 5 && path.length() >= 3 && isInit) {
							return Evaluation.INCLUDE_AND_CONTINUE;
						}else {
							return Evaluation.EXCLUDE_AND_CONTINUE;
						}
						//return ((path.length() <= 5 && path.length() >= 3) ? Evaluation.INCLUDE_AND_CONTINUE : Evaluation.EXCLUDE_AND_CONTINUE);
					}
				 } );
		
		private final PathExpander expander;
	    private final InitialBranchState stateFactory;
	    private final CostEvaluator<Double> costEvaluator;
	    private Traverser lastTraverser;

	    public MyDijkstra( PathExpander expander, CostEvaluator<Double> costEvaluator )
	    {
	        this( expander, InitialBranchState.NO_STATE, costEvaluator );
	    }

	    public MyDijkstra( PathExpander expander, InitialBranchState stateFactory, CostEvaluator<Double> costEvaluator )
	    {
	        this.expander = expander;
	        this.costEvaluator = costEvaluator;
	        this.stateFactory = stateFactory;
	    }

	    public MyDijkstra( RelationshipExpander expander, CostEvaluator<Double> costEvaluator )
	    {
	        this( toPathExpander( expander ), costEvaluator );
	    }

    @Override
    public Iterable<WeightedPath> findAllPaths( Node start, final Node end )
    {
        final Traverser traverser = traverser( start, end, true );
        return new Iterable<WeightedPath>()
        {
            @Override
            public Iterator<WeightedPath> iterator()
            {
                return new StopAfterWeightIterator( traverser.iterator(), costEvaluator );
            }
        };
    }

    private Traverser traverser( Node start, final Node end, boolean forMultiplePaths )
    {
        return (lastTraverser = TRAVERSAL.expand( expander, stateFactory )
                .order( new SelectorFactory( forMultiplePaths, costEvaluator ) )
                .evaluator( Evaluators.includeWhereEndNodeIs( end ) ).traverse( start ) );
    }

    @Override
    public WeightedPath findSinglePath( Node start, Node end )
    {
        return firstOrNull( new StopAfterWeightIterator( traverser( start, end, false ).iterator(), costEvaluator ) );
    }

    @Override
    public TraversalMetadata metadata()
    {
        return lastTraverser.metadata();
    }

    private static class SelectorFactory extends BestFirstSelectorFactory<Double, Double>
    {
        private final CostEvaluator<Double> evaluator;

        SelectorFactory( boolean forMultiplePaths, CostEvaluator<Double> evaluator )
        {
            super( forMultiplePaths );
            this.evaluator = evaluator;
        }

        @Override
        protected Double calculateValue( TraversalBranch next )
        {
            return next.length() == 0 ? 0d : evaluator.getCost(
                    next.lastRelationship(), Direction.OUTGOING );
        }

        @Override
        protected Double addPriority( TraversalBranch source,
                Double currentAggregatedValue, Double value )
        {
            return withDefault( currentAggregatedValue, 0d ) + withDefault( value, 0d );
        }

        private <T> T withDefault( T valueOrNull, T valueIfNull )
        {
            return valueOrNull != null ? valueOrNull : valueIfNull;
        }

        @Override
        protected Double getStartData()
        {
            return 0d;
        }
    }
}
