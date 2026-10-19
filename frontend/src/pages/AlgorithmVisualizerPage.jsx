import React, { useState, useEffect, useRef } from 'react';
import { api } from '../services/api';
import {
  GitGraph,
  Play,
  Pause,
  SkipBack,
  SkipForward,
  RotateCcw,
  Sparkles,
  Layers,
  Cpu,
  BookOpen,
  Info,
  CheckCircle2,
} from 'lucide-react';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

export const AlgorithmVisualizerPage = () => {
  const [graphData, setGraphData] = useState(null);
  const [sourceNode, setSourceNode] = useState('Depot');
  const [destNode, setDestNode] = useState('Terminal');
  const [algorithm, setAlgorithm] = useState('DIJKSTRA');

  const [traceResult, setTraceResult] = useState(null);
  const [currentStepIndex, setCurrentStepIndex] = useState(0);
  const [isPlaying, setIsPlaying] = useState(false);
  const [loading, setLoading] = useState(false);

  const timerRef = useRef(null);

  // Load sample graph on mount
  useEffect(() => {
    api.getSampleGraph()
      .then((data) => {
        setGraphData(data);
      })
      .catch((err) => console.error('Failed to load sample graph:', err));
  }, []);

  const runAlgorithmTrace = async () => {
    setLoading(true);
    setIsPlaying(false);
    clearInterval(timerRef.current);

    try {
      const payload = {
        graphPreset: 'SAMPLE_LOGISTICS_NETWORK',
        sourceNode,
        destinationNode: destNode,
        algorithm,
      };

      const result = algorithm === 'ASTAR'
        ? await api.traceAStar(payload)
        : await api.traceDijkstra(payload);

      setTraceResult(result);
      setCurrentStepIndex(0);
    } catch (err) {
      console.error('Failed to execute trace:', err);
      alert(err.message || 'Error executing algorithm trace');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (graphData) {
      runAlgorithmTrace();
    }
  }, [algorithm, sourceNode, destNode, graphData]);

  // Auto-play timer
  useEffect(() => {
    if (isPlaying && traceResult?.steps) {
      timerRef.current = setInterval(() => {
        setCurrentStepIndex((prev) => {
          if (prev >= traceResult.steps.length - 1) {
            setIsPlaying(false);
            clearInterval(timerRef.current);
            return prev;
          }
          return prev + 1;
        });
      }, 900);
    } else {
      clearInterval(timerRef.current);
    }

    return () => clearInterval(timerRef.current);
  }, [isPlaying, traceResult]);

  const currentStep = traceResult?.steps ? traceResult.steps[currentStepIndex] : null;

  // Determine node style based on current step
  const getNodeFill = (nodeId) => {
    if (!currentStep) return '#e2e8f0';

    if (currentStep.currentNode === nodeId) {
      return '#3b82f6'; // Active visited focus (bright blue)
    }
    if (currentStep.examiningNeighbor === nodeId) {
      return '#f97316'; // Currently being evaluated/relaxed (orange)
    }
    if (traceResult?.pathFound && currentStepIndex === traceResult.steps.length - 1 && traceResult.shortestPath?.includes(nodeId)) {
      return '#10b981'; // Final shortest path (emerald green)
    }
    if (currentStep.visitedNodes?.includes(nodeId)) {
      return '#0f172a'; // Settled visited node (navy dark)
    }
    return '#f1f5f9'; // Unvisited (light slate)
  };

  const getNodeTextColor = (nodeId) => {
    if (!currentStep) return '#1e293b';
    if (currentStep.currentNode === nodeId || currentStep.visitedNodes?.includes(nodeId) || (traceResult?.shortestPath?.includes(nodeId) && currentStepIndex === traceResult.steps.length - 1)) {
      return '#ffffff';
    }
    return '#0f172a';
  };

  // Determine edge styling
  const isEdgeInShortestPath = (src, tgt) => {
    if (!traceResult?.shortestPath || currentStepIndex !== traceResult.steps.length - 1) return false;
    const path = traceResult.shortestPath;
    for (let i = 0; i < path.length - 1; i++) {
      if ((path[i] === src && path[i + 1] === tgt) || (path[i] === tgt && path[i + 1] === src)) {
        return true;
      }
    }
    return false;
  };

  return (
    <div className="space-y-6 pb-12 font-sans">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight flex items-center gap-2">
            <GitGraph className="w-7 h-7 text-blue-600" />
            Algorithm Execution Visualizer
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Step-by-step technical demonstration of Dijkstra's algorithm and A* Search on a weighted graph.
          </p>
        </div>

        <div className="flex items-center space-x-3">
          <select
            value={algorithm}
            onChange={(e) => setAlgorithm(e.target.value)}
            className="px-3 py-2 bg-white border border-slate-300 rounded-xl text-xs font-bold text-slate-800 shadow-sm"
          >
            <option value="DIJKSTRA">Dijkstra's Algorithm</option>
            <option value="ASTAR">A* Search Algorithm</option>
          </select>
        </div>
      </div>

      {/* Control Toolbar */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm flex flex-wrap items-center justify-between gap-4">
        {/* Source & Destination selectors */}
        <div className="flex items-center space-x-3">
          <div className="flex items-center space-x-2 text-xs">
            <span className="font-semibold text-slate-600">Source:</span>
            <select
              value={sourceNode}
              onChange={(e) => setSourceNode(e.target.value)}
              className="px-2.5 py-1.5 border border-slate-200 rounded-lg bg-slate-50 font-bold text-slate-800 text-xs"
            >
              {graphData?.nodes?.map((n) => (
                <option key={n.id} value={n.id}>
                  {n.label} ({n.id})
                </option>
              ))}
            </select>
          </div>

          <span className="text-slate-400">&rarr;</span>

          <div className="flex items-center space-x-2 text-xs">
            <span className="font-semibold text-slate-600">Target:</span>
            <select
              value={destNode}
              onChange={(e) => setDestNode(e.target.value)}
              className="px-2.5 py-1.5 border border-slate-200 rounded-lg bg-slate-50 font-bold text-slate-800 text-xs"
            >
              {graphData?.nodes?.map((n) => (
                <option key={n.id} value={n.id}>
                  {n.label} ({n.id})
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Playback Controls */}
        <div className="flex items-center space-x-2">
          <button
            onClick={() => setCurrentStepIndex(0)}
            disabled={!traceResult?.steps || currentStepIndex === 0}
            className="p-2 border border-slate-200 hover:bg-slate-100 rounded-lg text-slate-600 disabled:opacity-40 transition-colors"
            title="Reset to Step 1"
          >
            <RotateCcw className="w-4 h-4" />
          </button>

          <button
            onClick={() => setCurrentStepIndex((p) => Math.max(0, p - 1))}
            disabled={!traceResult?.steps || currentStepIndex === 0}
            className="p-2 border border-slate-200 hover:bg-slate-100 rounded-lg text-slate-600 disabled:opacity-40 transition-colors"
            title="Step Back"
          >
            <SkipBack className="w-4 h-4" />
          </button>

          <button
            onClick={() => setIsPlaying(!isPlaying)}
            disabled={!traceResult?.steps || currentStepIndex >= traceResult.steps.length - 1}
            className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs font-semibold flex items-center space-x-1.5 shadow-sm transition-colors disabled:opacity-50"
          >
            {isPlaying ? (
              <>
                <Pause className="w-3.5 h-3.5" />
                <span>Pause</span>
              </>
            ) : (
              <>
                <Play className="w-3.5 h-3.5" />
                <span>Play</span>
              </>
            )}
          </button>

          <button
            onClick={() => setCurrentStepIndex((p) => Math.min(traceResult.steps.length - 1, p + 1))}
            disabled={!traceResult?.steps || currentStepIndex >= traceResult.steps.length - 1}
            className="p-2 border border-slate-200 hover:bg-slate-100 rounded-lg text-slate-600 disabled:opacity-40 transition-colors"
            title="Step Forward"
          >
            <SkipForward className="w-4 h-4" />
          </button>

          <span className="text-xs font-mono text-slate-500 pl-2">
            Step {currentStepIndex + 1} of {traceResult?.steps?.length || 1}
          </span>
        </div>
      </div>

      {/* Main Visualizer Canvas & Trace Step Details */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* SVG Graph Canvas (7 cols) */}
        <div className="lg:col-span-7 bg-white p-4 rounded-2xl border border-slate-200 shadow-sm flex flex-col justify-between">
          <div className="flex items-center justify-between pb-2 border-b border-slate-100 text-xs">
            <div className="flex items-center space-x-2 font-semibold text-slate-700">
              <span className="w-2.5 h-2.5 rounded-full bg-blue-600"></span>
              <span>Sample Logistics Transportation Network</span>
            </div>
            <div className="flex items-center space-x-3 text-[11px] text-slate-500">
              <span className="flex items-center gap-1">
                <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 inline-block"></span> Path
              </span>
              <span className="flex items-center gap-1">
                <span className="w-2.5 h-2.5 rounded-full bg-blue-600 inline-block"></span> Active
              </span>
              <span className="flex items-center gap-1">
                <span className="w-2.5 h-2.5 rounded-full bg-slate-900 inline-block"></span> Visited
              </span>
            </div>
          </div>

          {/* Render Graph SVG */}
          <div className="w-full h-[400px] flex items-center justify-center relative overflow-hidden bg-slate-50/50 rounded-xl mt-2 border border-slate-100">
            {graphData && (
              <svg viewBox="0 0 800 500" className="w-full h-full">
                {/* Edges */}
                {graphData.edges.map((edge) => {
                  const src = graphData.nodes.find((n) => n.id === edge.source);
                  const tgt = graphData.nodes.find((n) => n.id === edge.target);
                  if (!src || !tgt) return null;

                  const isPath = isEdgeInShortestPath(edge.source, edge.target);
                  const isExamining =
                    (currentStep?.currentNode === edge.source && currentStep?.examiningNeighbor === edge.target) ||
                    (currentStep?.currentNode === edge.target && currentStep?.examiningNeighbor === edge.source);

                  const midX = (src.x + tgt.x) / 2;
                  const midY = (src.y + tgt.y) / 2;

                  return (
                    <g key={edge.id}>
                      <line
                        x1={src.x}
                        y1={src.y}
                        x2={tgt.x}
                        y2={tgt.y}
                        stroke={isPath ? '#10b981' : isExamining ? '#f97316' : '#cbd5e1'}
                        strokeWidth={isPath ? 4.5 : isExamining ? 3.5 : 2}
                        strokeDasharray={isExamining ? '4, 4' : 'none'}
                        className="transition-colors duration-300"
                      />
                      {/* Weight badge */}
                      <rect
                        x={midX - 12}
                        y={midY - 9}
                        width="24"
                        height="18"
                        rx="4"
                        fill="#ffffff"
                        stroke="#94a3b8"
                        strokeWidth="1"
                      />
                      <text
                        x={midX}
                        y={midY + 4}
                        textAnchor="middle"
                        fontSize="10"
                        fontWeight="bold"
                        fill="#334155"
                      >
                        {edge.weight}
                      </text>
                    </g>
                  );
                })}

                {/* Nodes */}
                {graphData.nodes.map((node) => {
                  const fill = getNodeFill(node.id);
                  const textFill = getNodeTextColor(node.id);
                  const isCurrent = currentStep?.currentNode === node.id;

                  return (
                    <g key={node.id} className="cursor-pointer transition-transform duration-200">
                      {isCurrent && (
                        <circle
                          cx={node.x}
                          cy={node.y}
                          r="28"
                          fill="none"
                          stroke="#3b82f6"
                          strokeWidth="2.5"
                          opacity="0.6"
                          className="animate-ping"
                        />
                      )}
                      <circle
                        cx={node.x}
                        cy={node.y}
                        r="22"
                        fill={fill}
                        stroke="#ffffff"
                        strokeWidth="2.5"
                        className="transition-colors duration-300 shadow-md"
                      />
                      <text
                        x={node.x}
                        y={node.y + 4}
                        textAnchor="middle"
                        fontSize="10"
                        fontWeight="bold"
                        fill={textFill}
                      >
                        {node.id}
                      </text>
                      <text
                        x={node.x}
                        y={node.y + 36}
                        textAnchor="middle"
                        fontSize="11"
                        fontWeight="600"
                        fill="#475569"
                      >
                        {node.label}
                      </text>
                    </g>
                  );
                })}
              </svg>
            )}
          </div>
        </div>

        {/* Step Inspector & Priority Queue State (5 cols) */}
        <div className="lg:col-span-5 space-y-4">
          {/* Step Explanation Card */}
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm space-y-3">
            <div className="flex items-center justify-between text-xs">
              <span className="font-bold text-slate-800 uppercase tracking-wider">
                Step Trace Execution
              </span>
              <span className="font-mono text-blue-600 bg-blue-50 px-2 py-0.5 rounded font-bold">
                Step #{currentStep?.stepNumber || 1}
              </span>
            </div>

            <p className="text-xs text-slate-700 font-medium bg-slate-50 p-3 rounded-xl border border-slate-100 leading-relaxed min-h-[50px]">
              {currentStep?.description || 'Evaluating graph paths...'}
            </p>

            {/* Priority Queue Inspection */}
            <div>
              <div className="text-[11px] font-bold uppercase tracking-wider text-slate-500 mb-1.5">
                Priority Queue (Min-Heap Binary Tree) State:
              </div>
              <div className="flex flex-wrap gap-1.5 max-h-20 overflow-y-auto">
                {currentStep?.priorityQueueState && currentStep.priorityQueueState.length > 0 ? (
                  currentStep.priorityQueueState.map((entry, idx) => (
                    <span
                      key={idx}
                      className="px-2 py-0.5 rounded-lg bg-blue-50 text-blue-800 text-[11px] font-mono border border-blue-200"
                    >
                      {entry}
                    </span>
                  ))
                ) : (
                  <span className="text-slate-400 text-xs italic">Empty queue (all reachable nodes processed)</span>
                )}
              </div>
            </div>

            {/* Tentative Distances Table */}
            <div>
              <div className="text-[11px] font-bold uppercase tracking-wider text-slate-500 mb-1.5">
                Tentative Distance Table dist[v]:
              </div>
              <div className="grid grid-cols-4 gap-1.5 text-[11px] max-h-28 overflow-y-auto font-mono">
                {currentStep?.shortestDistances &&
                  Object.entries(currentStep.shortestDistances).map(([nodeId, dist]) => (
                    <div
                      key={nodeId}
                      className={`p-1.5 rounded border text-center ${
                        currentStep.currentNode === nodeId
                          ? 'bg-blue-600 text-white font-bold border-blue-600'
                          : dist < Infinity
                          ? 'bg-slate-100 text-slate-800 border-slate-200'
                          : 'bg-slate-50 text-slate-400 border-slate-100'
                      }`}
                    >
                      <div>{nodeId}</div>
                      <div>{dist === Infinity ? '∞' : dist.toFixed(1)}</div>
                    </div>
                  ))}
              </div>
            </div>

            {/* Current Shortest Path Candidate */}
            {traceResult && (
              <div className="pt-2 border-t border-slate-100 flex items-center justify-between text-xs">
                <span className="font-semibold text-slate-600">Optimal Path:</span>
                <span className="font-mono font-bold text-emerald-600">
                  {traceResult.shortestPath ? traceResult.shortestPath.join(' → ') : 'None'}
                </span>
                <span className="bg-emerald-50 text-emerald-700 px-2 py-0.5 rounded font-bold font-mono">
                  Cost: {traceResult.totalCost ?? 'N/A'}
                </span>
              </div>
            )}
          </div>

          {/* Technical Interview Complexity Card */}
          <div className="bg-gradient-to-br from-slate-900 to-slate-800 p-5 rounded-2xl text-white shadow-sm space-y-3">
            <div className="flex items-center space-x-2 text-xs font-bold text-blue-300 uppercase">
              <Cpu className="w-4 h-4 text-blue-400" />
              <span>Technical Complexity Analysis</span>
            </div>

            <div className="grid grid-cols-2 gap-3 text-xs">
              <div className="bg-slate-800/80 p-2.5 rounded-xl border border-slate-700">
                <span className="text-[10px] uppercase font-bold text-slate-400 block">Time Complexity:</span>
                <span className="font-mono font-bold text-amber-400 text-xs">
                  {traceResult?.complexity?.timeComplexity || 'O((|V| + |E|) log |V|)'}
                </span>
              </div>

              <div className="bg-slate-800/80 p-2.5 rounded-xl border border-slate-700">
                <span className="text-[10px] uppercase font-bold text-slate-400 block">Space Complexity:</span>
                <span className="font-mono font-bold text-sky-400 text-xs">
                  {traceResult?.complexity?.spaceComplexity || 'O(|V| + |E|)'}
                </span>
              </div>
            </div>

            <p className="text-[11px] text-slate-300 leading-relaxed">
              {traceResult?.complexity?.description ||
                'Dijkstra uses a binary min-heap to greedily expand the minimum confirmed distance edge, guaranteeing optimal shortest path on graphs with non-negative weights.'}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
