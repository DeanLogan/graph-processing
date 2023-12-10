import matplotlib.pyplot as plt

def create_graph(threads, values, title, x_label, y_label, legend_label, log_scale=False):
    # Create a line chart
    plt.plot(threads, values, marker='o', linestyle='-', color='b', label=legend_label)

    if(log_scale):
        plt.xscale('log')
    plt.xlabel(x_label)
    plt.ylabel(y_label)
    plt.title(title)
    plt.grid(True)
    plt.xticks(threads)
    plt.legend()
    plt.show()

def q2_graph():
    threads = list(range(1, 19))
    execution_speed = [
        79.1362417, 59.9532814, 55.9259617, 52.7513062,
        51.5374887, 50.9806836, 50.69486, 49.9483402,
        49.359525, 48.5931538, 48.2308547, 48.000364,
        47.932478, 47.2351162, 47.3311874, 47.012477,
        47.2318632, 47.142078836
    ]
    
    create_graph(threads, execution_speed, 'Program Execution Speed vs Number of Threads', 'Number of Threads', 'Speed of Program Execution', 'Execution Speed')

def q3_graph_execution():
    threads = list(range(1, 19))
    execution_speed = [
        14.6703652 , 13.2025944, 9.54236275, 9.4785429,
        9.2574753, 9.2234761, 9.160707, 9.218437,
        9.2337974, 9.2109057, 9.2453078, 9.236831,
        9.1952057, 9.1708166, 9.1336068,  9.09682624,
        9.1086117, 9.1332665 
    ]
    
    create_graph(threads, execution_speed, 'Program Execution Speed vs Number of Threads', 'Number of Threads', 'Speed of Program Execution', 'Execution Speed')

def q3_graph_speedup():
    threads = list(range(1, 19))
    speedup_values = [
        1.0, 1.11164085341, 1.53851306439, 1.54583947847,
        1.58951170882, 1.59477125746, 1.603265787,
        1.59148241426, 1.58863728372, 1.59072597362,
        1.58643214589, 1.58752476379, 1.59309084299,
        1.59710020998, 1.60575904003, 1.61538554266,
        1.59439348274, 1.59004056774
    ]
    
    create_graph(threads, speedup_values, 'Speedup vs Number of Threads', 'Number of Threads', 'Speedup Compared to Single Thread', 'Speedup')

def q4_graph_block_size_execution():
    block_size = [
        1, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 
        131072, 262144, 524288, 1048576, 2097152, 4194304, 8388608
    ]

    execution_speed = [
        44.0663816, 14.6230084, 14.0730901,
        14.1942411, 13.5565326, 13.3722779,
        13.3155204, 13.2820387,  13.2353412,
        13.25509170, 13.3373671, 13.3380625,
        13.3762035, 13.35910790,  13.3650236,
        14.0544914, 15.6396729, 18.4205845
    ]

    create_graph(block_size, execution_speed, 'Program Execution Speed vs Block Size', 'Block Size', 'Speed of Program Execution', 'Execution Speed', True)

def q4_graph_block_size_speedup():
    block_size = [
        1, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 
        131072, 262144, 524288, 1048576, 2097152, 4194304, 8388608
    ]

    speedup = [
        1, 3.0146416, 3.1267432, 3.0989488, 
        3.2528069, 3.2948543, 3.3182224, 
        3.3295749, 3.3499498, 3.3446806, 
        3.3139201, 3.3137614, 3.3035717,
        3.3087008, 3.3070993, 3.1342808,
        2.8168133, 2.3965318
    ]

    create_graph(block_size, speedup, 'Speedup Of Block Size', 'Block Size', 'Speedup Compared to Block Size of 1', 'Speedup', True)

def q4_graph_buffer_size_execution():
    buffer_size = [ 1, 2, 3, 4, 5, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536]

    execution_speed = [
        15.4273168, 14.8454192, 14.7634755,
        14.7443694, 14.6812155, 14.5762844,
        14.5640506, 14.5853761,  14.6065530,
        14.5505093, 14.6611823, 14.6140866,
        14.5614325, 14.5604498, 14.6607668
    ]

    create_graph(buffer_size, execution_speed, 'Program Execution Speed vs Buffer Size', 'Buffer Size', 'Speed of Program Execution', 'Execution Speed', True)

def q4_graph_buffer_size_speedup():
    buffer_size = [ 1, 2, 3, 4, 5, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536]

    speedup = [
        1, 1.0460, 1.0448, 1.0461,
        1.0508, 1.0598, 1.0589,
        1.0592, 1.0589, 1.0577,
        1.0551, 1.0575, 1.0595,
        1.0596, 1.0551
    ]

    create_graph(buffer_size, speedup, 'Speedup Of Buffer Size', 'Buffer Size', 'Speedup Compared to Buffer Size of 1', 'Speedup', True)

if __name__ == '__main__':
    q4_graph_buffer_size_speedup()
