using UnityEngine;

namespace ProjectRelay.UI
{
    public class AudioNavigationCue : MonoBehaviour
    {
        [SerializeField] private Transform player;
        [SerializeField] private Transform target;
        [SerializeField] private AudioSource cueSource;
        [SerializeField] private float maxDistance = 40f;
        [SerializeField] private float minPitch = 0.8f;
        [SerializeField] private float maxPitch = 1.3f;

        private void Update()
        {
            if (player == null || target == null || cueSource == null) return;

            Vector3 toTarget = target.position - player.position;
            float dist = toTarget.magnitude;
            float normalized = Mathf.Clamp01(1f - (dist / maxDistance));

            // 간단한 방향 큐: 플레이어 기준 좌우 패닝
            Vector3 localDir = player.InverseTransformDirection(toTarget.normalized);
            cueSource.panStereo = Mathf.Clamp(localDir.x, -1f, 1f);

            cueSource.volume = Mathf.Lerp(0.1f, 1f, normalized);
            cueSource.pitch = Mathf.Lerp(minPitch, maxPitch, normalized);
        }
    }
}
